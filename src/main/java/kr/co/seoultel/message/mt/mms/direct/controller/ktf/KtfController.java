package kr.co.seoultel.message.mt.mms.direct.controller.ktf;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;


import kr.co.seoultel.message.mt.mms.core.common.protocol.KtfProtocol;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfResMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import jakarta.xml.soap.SOAPException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.PostMapping;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import org.springframework.web.bind.annotation.RestController;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.filter.CachedHttpServletRequest;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.direct.ktf.KtfCondition;
import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfMMSReportUtil;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;

@Slf4j
@RestController
@RequiredArgsConstructor
@Conditional(KtfCondition.class)
public class KtfController {

    private final KtfMMSReportUtil ktfMMSReportUtil = new KtfMMSReportUtil();

    private final PersistenceManager persistenceManager;
    private final ConcurrentLinkedQueue<MrReport> reportQueue;
    private final ConcurrentLinkedQueue<MessageDelivery> republishQueue;

    /*
     * DeliveryReportReq
     */
    @PostMapping("")
    public ResponseEntity<String> receiveMM7DeliveryReportReq(HttpServletRequest httpServletRequest) throws IOException, SOAPException, PersistenceException {
        CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest(httpServletRequest);
        String xml = StreamUtils.copyToString(cachedHttpServletRequest.getInputStream(), Charset.forName(EUC_KR));

        KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = new KtfDeliveryReportReqMessage();
        ktfDeliveryReportReqMessage.fromXml(xml);

        String tid = ktfDeliveryReportReqMessage.getTid();
        String dstMsgId = ktfDeliveryReportReqMessage.getMessageId();
        String statusCode = ktfDeliveryReportReqMessage.getMmStatus();
        String callback = ktfDeliveryReportReqMessage.getCallback();
        String receiver = ktfDeliveryReportReqMessage.getReceiver();
        String timeStamp = ktfDeliveryReportReqMessage.getTimeStamp();

        KtfResMessage ktfResMessage = new KtfResMessage(KtfProtocol.DELIVERY_REPORT_RES);
        ktfResMessage.setTid(tid);
        ktfResMessage.setMessageId(dstMsgId);
        ktfResMessage.setStatusCode("1000");
        ktfResMessage.setStatusText("Success");

        log.info("[REPORT] Successfully received Report[{}] from KTF", ktfDeliveryReportReqMessage);

        // HASH-MAP | REDIS에 메세지가 존재하는지 확인한다.
        Optional<MessageDelivery> opt = persistenceManager.findMessageByUmsMsgId(dstMsgId);
        MessageDelivery messageDelivery = opt.orElseThrow(() -> new PersistenceException(ktfDeliveryReportReqMessage));

        if (ktfDeliveryReportReqMessage.isTpsOver()) {
            republishQueue.add(messageDelivery);
        } else {
            ktfMMSReportUtil.prepareToReport(messageDelivery, ktfDeliveryReportReqMessage);

            DeliveryType deliveryType = FallbackUtil.isFallback(messageDelivery) ? DeliveryType.FALLBACK_REPORT : DeliveryType.REPORT;
            MrReport mrReport = new MrReport(deliveryType, messageDelivery);
            reportQueue.add(mrReport);

            log.info("[REPORT] Successfully add Report[{}] in reportQueue", ktfDeliveryReportReqMessage);
        }

        return createResponseEntity(ktfResMessage.convertSOAPMessageToString());
    }

    private ResponseEntity<String> createResponseEntity(String requestBody) {
        // 헤더 설정
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/xml; charset=\"euc-kr\"");
        headers.add(Constants.SOAP_ACTION, "\"\"");

        return new ResponseEntity<>(requestBody, headers, HttpStatus.OK);
    }
}
