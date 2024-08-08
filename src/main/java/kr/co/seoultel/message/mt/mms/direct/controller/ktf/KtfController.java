package kr.co.seoultel.message.mt.mms.direct.controller.ktf;


import java.io.IOException;
import java.util.Optional;


import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportResMessage;
import kr.co.seoultel.message.mt.mms.core.util.ConvertorUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.RedisUtil;
import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.PostMapping;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.ktf.KtfCondition;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfMMSReportUtil;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;

@Slf4j
@RestController
@RequiredArgsConstructor
@Conditional(KtfCondition.class)
public class KtfController {

    protected final RedisService redisService;

    protected final QueueStorage<MrReport> reportQueueStorage;
    protected final QueueStorage<MessageDelivery> republishQueueStorage;
    protected final HashMapStorage<String, MessageDelivery> deliveryStorage;
    private final KtfMMSReportUtil ktfMMSReportUtil = new KtfMMSReportUtil();

    /*
     * DeliveryReportReq
     * TODO : VaspErrorRsp 또는 RsErrorRsp 를 수신받는 경우, MessageID가 존재하지 않아 DeliveryReport.RES 를 전달할 수 없음..
     *        이 경우 예외 발생하나 어떻게 처리해야할 지 의문.
     */
    @PostMapping(value = {"/", ""}, consumes = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> receiveMM7DeliveryReportReq(@RequestBody String xml) throws IOException, MCMPSoapRenderException, PersistenceException {
        try {
            log.info("[REPORT's XML] Successfully received origin xml : {}", xml);

            KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = new KtfDeliveryReportReqMessage();
            ktfDeliveryReportReqMessage.fromXml(xml.trim());
            log.info("[RECEIVED-REPORT] Successfully received Report[{}] from KTF", ktfDeliveryReportReqMessage);

            String tid = ktfDeliveryReportReqMessage.getTid();
            String messageId = ktfDeliveryReportReqMessage.getMessageId();

            // HASH-MAP | REDIS에 메세지가 존재하는지 확인한다.
            MessageDelivery messageDelivery = Optional.ofNullable(deliveryStorage.get(messageId)).orElseThrow(() -> new PersistenceException(ktfDeliveryReportReqMessage));
            messageDelivery.setDstMsgId(messageId);

            if (ktfDeliveryReportReqMessage.isTpsOver()) {
                republishQueueStorage.add(messageDelivery);
            } else {
                ktfMMSReportUtil.prepareToReport(messageDelivery, ktfDeliveryReportReqMessage);

                DeliveryType deliveryType = FallbackUtil.isFallback(messageDelivery) ? DeliveryType.FALLBACK_REPORT : DeliveryType.REPORT;
                MrReport mrReport = new MrReport(deliveryType, messageDelivery);
                reportQueueStorage.add(mrReport);

                log.info("[QUEUE] Successfully add Report[{}] in reportQueue", ktfDeliveryReportReqMessage);
            }

            KtfDeliveryReportResMessage ktfDeliveryReportResMessage = KtfDeliveryReportResMessage.builder()
                                                                                                 .tid(tid)
                                                                                                 .messageId(messageId)
                                                                                                 .statusCode("1000")
                                                                                                 .statusText("Success")
                                                                                                 .build();

            return createResponseEntity(ktfDeliveryReportResMessage.convertSOAPMessageToString());
        } catch (PersistenceException exception) {
            return handlePersistenceException(exception);
        }
    }

    private ResponseEntity<String> handlePersistenceException(PersistenceException exception) throws MCMPSoapRenderException {
        KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = (KtfDeliveryReportReqMessage) ((PersistenceException) exception).getSource();

        String tid = ktfDeliveryReportReqMessage.getTid();
        String messageId = ktfDeliveryReportReqMessage.getMessageId();

        Optional<String> optional = redisService.getSafely(RedisUtil.getRedisKeyOfMessage(), messageId);
        if (optional.isPresent()) {
            MessageDelivery messageDelivery = ConvertorUtil.convertJsonToObject(optional.get(), MessageDelivery.class);
            deliveryStorage.putIfAbsent(messageId, messageDelivery);
            log.warn("[SYSTEM] Re-add MessageDelivery[{}] in redis to deliveryStorage", messageDelivery);

            KtfDeliveryReportResMessage ktfDeliveryReportResMessage = KtfDeliveryReportResMessage.builder()
                                                                                                 .tid(tid)
                                                                                                 .messageId(messageId)
                                                                                                 .statusCode("9999")
                                                                                                 .statusText("UNDEFINED")
                                                                                                 .build();

            return createResponseEntity(ktfDeliveryReportResMessage.convertSOAPMessageToString());
        } else {
            log.error("[SYSTEM] Fail to find message[dstMsgId : {}] in redis, received request[{}]", messageId, ktfDeliveryReportReqMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server Error");
        }
    }

    private ResponseEntity<String> createResponseEntity(String requestBody) {
        // 헤더 설정
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml; charset=\"euc-kr\"");
        headers.add(Constants.SOAP_ACTION, "\"\"");

        return new ResponseEntity<>(requestBody, headers, HttpStatus.OK);
    }
}
