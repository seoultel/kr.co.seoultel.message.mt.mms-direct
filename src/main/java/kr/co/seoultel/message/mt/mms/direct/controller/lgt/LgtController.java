package kr.co.seoultel.message.mt.mms.direct.controller.lgt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtDeliveryReportResMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.ConvertorUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.RedisUtil;
import kr.co.seoultel.message.mt.mms.direct.util.lgt.LgtMMSReportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import kr.co.seoultel.message.mt.mms.direct.lgt.LgtCondition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@Conditional(LgtCondition.class)
public class LgtController {

    private final RedisService redisService;

    protected final QueueStorage<MrReport> reportQueueStorage;
    protected final QueueStorage<MessageDelivery> republishQueueStorage;
    protected final HashMapStorage<String, MessageDelivery> deliveryStorage;

    private final LgtMMSReportUtil lgtMMSReportUtil = new LgtMMSReportUtil();

    @PostMapping(value = {"/", ""}, consumes = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> receiveMM7DeliveryReportReq(@RequestBody String xml) throws IOException, MCMPSoapRenderException, PersistenceException {
        try {
            log.info("[REPORT's XML] Successfully received origin xml : {}", xml);

            LgtDeliveryReportReqMessage lgtDeliveryReportReqMessage = new LgtDeliveryReportReqMessage();
            lgtDeliveryReportReqMessage.fromXml(xml.trim());
            log.info("[RECEIVED-REPORT] Successfully received Report[{}] from LGT", lgtDeliveryReportReqMessage);

            String tid = lgtDeliveryReportReqMessage.getTid();
            String messageId = lgtDeliveryReportReqMessage.getMessageId();

            // HASH-MAP | REDIS에 메세지가 존재하는지 확인한다.
            MessageDelivery messageDelivery = Optional.ofNullable(deliveryStorage.get(messageId)).orElseThrow(() -> new PersistenceException(lgtDeliveryReportReqMessage));
            messageDelivery.setDstMsgId(messageId);

            if (lgtDeliveryReportReqMessage.isTpsOver()) {
                republishQueueStorage.add(messageDelivery);
            } else {
                lgtMMSReportUtil.prepareToReport(messageDelivery, lgtDeliveryReportReqMessage);

                DeliveryType deliveryType = FallbackUtil.isFallback(messageDelivery) ? DeliveryType.FALLBACK_REPORT : DeliveryType.REPORT;
                MrReport mrReport = new MrReport(deliveryType, messageDelivery);
                reportQueueStorage.add(mrReport);

                log.info("[QUEUE] Successfully add Report[{}] in reportQueue", lgtDeliveryReportReqMessage);
            }

            LgtDeliveryReportResMessage lgtDeliveryReportResMessage = LgtDeliveryReportResMessage.builder()
                                                                                                 .tid(tid)
                                                                                                 .messageId(messageId)
                                                                                                 .statusCode("1000")
                                                                                                 .statusText("Success")
                                                                                                 .build();

            return createResponseEntity(lgtDeliveryReportResMessage.convertSOAPMessageToString());
        } catch (PersistenceException exception) {
            return handlePersistenceException(exception);
        }
    }


    private ResponseEntity<String> handlePersistenceException(PersistenceException exception) throws MCMPSoapRenderException {
        LgtDeliveryReportReqMessage lgtDeliveryReportReqMessage = (LgtDeliveryReportReqMessage) ((PersistenceException) exception).getSource();
        String tid = lgtDeliveryReportReqMessage.getTid();
        String messageId = lgtDeliveryReportReqMessage.getMessageId();

        Optional<String> optional = redisService.getSafely(RedisUtil.getRedisKeyOfMessage(), messageId);
        if (optional.isPresent()) {
            MessageDelivery messageDelivery = ConvertorUtil.convertJsonToObject(optional.get(), MessageDelivery.class);
            deliveryStorage.putIfAbsent(messageId, messageDelivery);
            log.warn("[SYSTEM] Re-add MessageDelivery[{}] in redis to deliveryStorage", messageDelivery);

            LgtDeliveryReportResMessage lgtDeliveryReportResMessage = LgtDeliveryReportResMessage.builder()
                                                                                                 .tid(tid)
                                                                                                 .messageId(messageId)
                                                                                                 .statusCode("1000")
                                                                                                 .statusText("Success")
                                                                                                 .build();

            return createResponseEntity(lgtDeliveryReportResMessage.convertSOAPMessageToString());
        } else {
            log.error("[SYSTEM] Fail to find message[dstMsgId : {}] in redis, received request[{}]", messageId, lgtDeliveryReportReqMessage);
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
