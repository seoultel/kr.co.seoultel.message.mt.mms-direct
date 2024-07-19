package kr.co.seoultel.message.mt.mms.direct.controller.skt;

import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportResMessage;
import kr.co.seoultel.message.mt.mms.core.util.ConvertorUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.RedisUtil;
import kr.co.seoultel.message.mt.mms.direct.filter.CachedHttpServletRequest;
import kr.co.seoultel.message.mt.mms.direct.util.skt.SktMMSReportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import kr.co.seoultel.message.mt.mms.direct.skt.SktCondition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
@Controller
@RequiredArgsConstructor
@Conditional(SktCondition.class)
public class SktController {
    private final SktMMSReportUtil sktMMSReportUtil = new SktMMSReportUtil();

    protected final RedisService redisService;
    protected final QueueStorage<MrReport> reportQueueStorage;
    protected final QueueStorage<MessageDelivery> republishQueueStorage;
    protected final HashMapStorage<String, MessageDelivery> deliveryStorage;

    /*
     * TODO : 예외 처리 어떻게할까 ?
     */
    @PostMapping("")
    public ResponseEntity<String> receiveMM7DeliveryReportReq(HttpServletRequest httpServletRequest) throws IOException, MCMPSoapRenderException, PersistenceException {
        CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest(httpServletRequest);
        String xml = StreamUtils.copyToString(cachedHttpServletRequest.getInputStream(), Charset.forName(EUC_KR));

        SktDeliveryReportReqMessage sktDeliveryReportReqMessage = new SktDeliveryReportReqMessage();
        sktDeliveryReportReqMessage.fromXml(xml);

        log.info("[REPORT] Successfully received Report[{}] from SKT", sktDeliveryReportReqMessage);

        String tid = sktDeliveryReportReqMessage.getTid();
        String messageId = sktDeliveryReportReqMessage.getMessageId();
        String receiver = sktDeliveryReportReqMessage.getReceiver();
        String timeStamp = sktDeliveryReportReqMessage.getTimeStamp();

        // HASH-MAP | REDIS에 메세지가 존재하는지 확인한다.
        MessageDelivery messageDelivery = Optional.ofNullable(deliveryStorage.get(messageId)).orElseThrow(() -> new PersistenceException(sktDeliveryReportReqMessage));

        // TODO : PersistenceException 발생했을 때 레디스에서 데이터가 존재한다면 statusCode랑 statusText 다르게 전송해야하지 않나.
        //        예외가 아니라 바로 레디스에서 가져와도 좋을듯
        SktDeliveryReportResMessage sktDeliveryReportResMessage = SktDeliveryReportResMessage.builder()
                .tid(tid)
                .statusCode("1000")
                .statusText("Success")
                .build();

        if (sktDeliveryReportReqMessage.isTpsOver()) {
            republishQueueStorage.add(messageDelivery);
        } else {
            sktMMSReportUtil.prepareToReport(messageDelivery, sktDeliveryReportReqMessage);

            DeliveryType deliveryType = FallbackUtil.isFallback(messageDelivery) ? DeliveryType.FALLBACK_REPORT : DeliveryType.REPORT;
            MrReport mrReport = new MrReport(deliveryType, messageDelivery);

            reportQueueStorage.add(mrReport);

            log.info("[REPORT-QUEUE] Successfully add Report[{}] in reportQueue", sktDeliveryReportReqMessage);
        }

        return createResponseEntity(sktDeliveryReportResMessage.convertSOAPMessageToString());

    }

    private ResponseEntity<String> createResponseEntity(String requestBody) {
        // 헤더 설정
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml; charset=\"euc-kr\"");
        headers.add(Constants.SOAP_ACTION, "\"\"");

        return new ResponseEntity<>(requestBody, headers, HttpStatus.OK);
    }
}
