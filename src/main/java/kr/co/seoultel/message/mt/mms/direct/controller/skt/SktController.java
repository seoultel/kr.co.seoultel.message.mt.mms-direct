package kr.co.seoultel.message.mt.mms.direct.controller.skt;

import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportResMessage;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
@Controller
@RequiredArgsConstructor
@Conditional(SktCondition.class)
public class SktController {
    private final SktMMSReportUtil sktMMSReportUtil = new SktMMSReportUtil();

    private final PersistenceManager persistenceManager;
    private final ConcurrentLinkedQueue<MrReport> reportQueue;
    private final ConcurrentLinkedQueue<MessageDelivery> republishQueue;

    /*
     * TODO : 예외 처리 어떻게할까 ?
     */
    @PostMapping("")
    public ResponseEntity<String> receiveMM7DeliveryReportReq(HttpServletRequest httpServletRequest) throws IOException, SOAPException, PersistenceException {
        CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest(httpServletRequest);
        String xml = StreamUtils.copyToString(cachedHttpServletRequest.getInputStream(), Charset.forName(EUC_KR));

        SktDeliveryReportReqMessage sktDeliveryReportReqMessage = new SktDeliveryReportReqMessage();
        sktDeliveryReportReqMessage.fromXml(xml);

        log.info("[REPORT] Successfully received Report[{}] from SKT", sktDeliveryReportReqMessage);

        String tid = sktDeliveryReportReqMessage.getTid();
        String dstMsgId = sktDeliveryReportReqMessage.getMessageId();
        String receiver = sktDeliveryReportReqMessage.getReceiver();
        String timeStamp = sktDeliveryReportReqMessage.getTimeStamp();

        SktDeliveryReportResMessage sktDeliveryReportResMessage = SktDeliveryReportResMessage.builder()
                                                                                             .tid(tid)
                                                                                             .statusCode("1000")
                                                                                             .statusText("Success")
                                                                                             .build();

        // HASH-MAP | REDIS에 메세지가 존재하는지 확인한다.
        Optional<MessageDelivery> opt = persistenceManager.findMessageByUmsMsgId(dstMsgId);
        MessageDelivery messageDelivery = opt.orElseThrow(() -> new PersistenceException(sktDeliveryReportReqMessage));

        if (sktDeliveryReportReqMessage.isTpsOver()) {
            republishQueue.add(messageDelivery);
        } else {
//            ktfMMSReportUtil.prepareToReport(messageDelivery, ktfDeliveryReportReqMessage);

            DeliveryType deliveryType = FallbackUtil.isFallback(messageDelivery) ? DeliveryType.FALLBACK_REPORT : DeliveryType.REPORT;
            MrReport mrReport = new MrReport(deliveryType, messageDelivery);
            reportQueue.add(mrReport);

            log.info("[REPORT] Successfully add Report[{}] in reportQueue", sktDeliveryReportReqMessage);
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
