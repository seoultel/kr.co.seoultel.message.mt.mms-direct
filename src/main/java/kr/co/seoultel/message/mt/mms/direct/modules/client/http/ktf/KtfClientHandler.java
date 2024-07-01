package kr.co.seoultel.message.mt.mms.direct.modules.client.http.ktf;

import jakarta.mail.MessagingException;
import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfProtocol;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfResMessage;
import kr.co.seoultel.message.mt.mms.core.util.*;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientHandler;

import kr.co.seoultel.message.mt.mms.direct.modules.client.http.ktf.util.KtfSoapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
public class KtfClientHandler extends HttpClientHandler {

    protected final KtfEndpoint endpoint;
    protected final KtfSoapUtil soapUtil;
//
    protected final RestTemplate restTemplate = new RestTemplateBuilder()
                                                    .defaultHeader(HttpHeaders.ACCEPT_CHARSET, EUC_KR)
                                                    .defaultHeader(HttpHeaders.USER_AGENT, "HUB")
                                                    .defaultHeader("SOAPAction", Constants.SOAP_ACTION)
                                                    .build();


    public KtfClientHandler(HttpClientProperty property, PersistenceManager persistenceManager, ConcurrentLinkedQueue<MrReport> reportQueue) {
        super(property, persistenceManager, reportQueue);

        this.soapUtil = new KtfSoapUtil(property);
        this.endpoint = new KtfEndpoint(property);
    }

    @Override
    protected void doSubmit(InboundMessage inboundMessage) throws IOException, NAckException, MessagingException, SOAPException {
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();
        String soapMessageToString = soapUtil.createSOAPMessage(messageDelivery);

        /* Request of submit */
        HttpEntity<String> httpEntity = getSubmitHttpEntity(soapMessageToString);
        ResponseEntity<String> response = restTemplate.exchange(endpoint.getHttpUrl(),
                                                                HttpMethod.POST,
                                                                httpEntity,
                                                                String.class);

        String xml = response.getBody();
        String localPart = KtfSoapUtil.getSOAPMessageMM7LocalPart(xml);

        KtfResMessage ktfResMessage = new KtfResMessage(localPart);
        ktfResMessage.fromXml(xml);

        String statusCode = ktfResMessage.getStatusCode();
        String statusText = ktfResMessage.getStatusText();
        switch (statusCode) {
            case KtfProtocol.KTF_SUBMIT_ACK_SUCCESS_RESULT:
                String dstMsgId = ktfResMessage.getMessageId();
                messageDelivery.setDstMsgId(dstMsgId);

                log.info("[SUBMIT_ACK & SUBMIT SUCCESS] Successfully received SubmitAck of message[{}] from {}", ktfResMessage, SenderConfig.TELECOM);

                persistenceManager.saveMessageByUmsMsgId(dstMsgId, messageDelivery);
                MrReport mrReport = new MrReport(DeliveryType.SUBMIT_ACK, messageDelivery);
                reportQueue.add(mrReport);

                log.info("[SUBMIT-ACK] Successfully add Report[{}] in reportQueue", ktfResMessage);
                inboundMessage.basicAck();
                break;

            case KtfProtocol.KTF_SUBMIT_ACK_TRANID_DUPLICATE_ERROR_RESULT:
                log.warn("[SUBMIT_ACK & DUPLICATED] Successfully received SubmitAck of message[{}] from {}",  ktfResMessage, SenderConfig.TELECOM);
                inboundMessage.basicAck();
                break;


            case KtfProtocol.KTF_SUBMIT_ACK_HUB_OVER_INTRAFFIC_RESULT:
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_AUTH_ERROR_RESULT:  //  HUBSP 인증 오류
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_NOTFOUND_RESULT:    //  HUBSP 없음 오류
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_BLOCK_RESULT:       //  HUBSP 정지 오류
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_EXPIRED_RESULT:     //  HUBSP 폐기 오류
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_IP_INVALID_RESULT:  //  HUBSP IP 오류
                log.error("[SUBMIT_ACK | FAILED] Successfully received SubmitAck of message[{}] from {}",
                        ktfResMessage, SenderConfig.TELECOM);

                CommonUtil.doThreadSleep(DateUtil.getTimeGapUntilNextSecond());
                inboundMessage.basicNack();
                break;

            default:
                log.error("[SUBMIT_ACK | FAILED] Successfully received SubmitAck of message[{}] from {}",
                        ktfResMessage, SenderConfig.TELECOM);
                inboundMessage.basicAck();
                break;
        }
    }



    private HttpEntity<String> getSubmitHttpEntity(String soapMessageToString) {
        HttpHeaders headers = new HttpHeaders();
        String httpBoundary = soapUtil.getHttpBoundary(soapMessageToString);
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/related; boundary=\"" + httpBoundary + "\"; " +
                "type=\"text/xml\"; charset=\"EUC-KR\"; start=\"" + Constants.KTF_CONTENT_ID + "\"");
        return new HttpEntity<>(soapMessageToString, headers);
    }
}