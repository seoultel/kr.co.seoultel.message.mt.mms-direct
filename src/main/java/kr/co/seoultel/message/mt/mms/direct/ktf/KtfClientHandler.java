package kr.co.seoultel.message.mt.mms.direct.ktf;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.common.protocol.KtfProtocol;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfResMessage;
import kr.co.seoultel.message.mt.mms.core.util.*;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientHandler;

import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfMMSReportUtil;
import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfSoapUtil;
import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
public class KtfClientHandler extends HttpClientHandler {
    protected final RestTemplate restTemplate = new RestTemplateBuilder()
                                                    .defaultHeader(HttpHeaders.ACCEPT_CHARSET, EUC_KR)
                                                    .defaultHeader(HttpHeaders.USER_AGENT, "HUB")
                                                    .defaultHeader("SOAPAction", Constants.SOAP_ACTION)
                                                    .build();


    protected final KtfEndpoint endpoint;
    protected final KtfSoapUtil soapUtil;

    protected final KtfMMSReportUtil ktfMMSReportUtil = new KtfMMSReportUtil();


    public KtfClientHandler(HttpClientProperty property, HashMapStorage<String, MessageDelivery> deliveryStorage, QueueStorage<MrReport> reportQueueStorage) {
        super(property, deliveryStorage, reportQueueStorage);

        this.soapUtil = new KtfSoapUtil(property);
        this.endpoint = new KtfEndpoint(property);
    }

    /*
     * TODO : 이통사로 Http 요청을 보내는 경우 예외 발생 시 NACK 전송을 위해서 return; 이 들어간 부분이 있음.
     *        예외 처리를 해당 메서드 내부에서 잡아야 하는 이유가 있는지.
     *        Aspect 사용하여 예외 처리할 때 발생하는 문제가 뭐가 있을지 검토.
     */
    @Override
    protected void doSubmit(InboundMessage inboundMessage) throws MCMPSoapRenderException, NAckException {
        String soapMessageToString = soapUtil.createSOAPMessage(inboundMessage);
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();
        String umsMsgId = messageDelivery.getUmsMsgId();

        ktfMMSReportUtil.prepareToSubmit(FallbackUtil.isFallback(messageDelivery), messageDelivery);

        try {
            /* Request of submit */
            HttpEntity<String> httpEntity = getSubmitHttpEntity(soapMessageToString);
            ResponseEntity<String> response = restTemplate.exchange(endpoint.getHttpUrl(),
                                                                    HttpMethod.POST,
                                                                    httpEntity,
                                                                    String.class);

            String xml = response.getBody();
            assert xml != null;

            String localPart = KtfSoapUtil.getSOAPMessageMM7LocalPart(xml);

            KtfResMessage ktfResMessage = new KtfResMessage(localPart);
            ktfResMessage.fromXml(xml);

            String messageId = ktfResMessage.getMessageId();
            String statusCode = ktfResMessage.getStatusCode();
            String statusText = ktfResMessage.getStatusText();
            messageDelivery.setDstMsgId(messageId);

            NAckType nAckType = KtfUtil.getNAckTypeBySubmitAckStatusCode(statusCode);
            if (nAckType.equals(NAckType.ACK)) {
                ktfMMSReportUtil.prepareToSubmitAck(messageDelivery, ktfResMessage);
                MessageDelivery cloneDelivery = (MessageDelivery) messageDelivery.clone();
                switch (statusCode) {
                    case KtfProtocol.KTF_SUBMIT_ACK_SUCCESS_RESULT:
                        log.info("[SUBMIT_ACK | SUCCESS] Successfully received SubmitAck of message[{}] from KTF", cloneDelivery);
                        deliveryStorage.put(messageId, cloneDelivery);
                        break;

                    default:
                        log.info("[SUBMIT_ACK & FAIL] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from KTF", ktfResMessage, umsMsgId);
                        break;
                }

                MrReport mrReport = new MrReport(DeliveryType.SUBMIT_ACK, cloneDelivery);
                reportQueueStorage.add(mrReport);
                log.info("[REPORT-QUEUE] Successfully add SubmitAck[{}] in reportQueue", ktfResMessage);

                inboundMessage.basicAck();
                return;
            } else {
                if (ktfResMessage.isTpsOver()) {
                    log.warn("[SUBMIT_ACK & TPS OVER] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from KTF", ktfResMessage, umsMsgId);
                    CommonUtil.doThreadSleep(DateUtil.getTimeGapUntilNextSecond());
                } else if (ktfResMessage.isHubspError()) {
                    log.error("[SUBMIT_ACK & HUBSP] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from KTF", ktfResMessage, umsMsgId);
                    CommonUtil.doThreadSleep(Constants.SECOND);
                } else {
                    log.warn("[SUBMIT_ACK | FAIL] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from KTF", ktfResMessage, umsMsgId);
                }

                // send nack to rabbitmq
                inboundMessage.basicNack();
                return;
            }
        }
        // 4xx 번대 예외 발생 시 해당 예외 처리 블럭으로 들어온다.
        catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getCause() instanceof BadRequestException) {
                log.error("[SUBMIT] Fail to send MM7_submit.REQ. It's likely to be wrong format error, check MM7_submit.REQ packet format", e);
            } else {
                log.error("[SUBMIT] Fail to send MM7_submit.REQ, requeue message[{}]", messageDelivery, e);
            }

            CommonUtil.doThreadSleep(1000L);
        }
        // 5xx 번대 예외 발생 시 해당 예외 처리 블럭으로 들어온다.
        catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("[SUBMIT] Fail to send MM7_submit.REQ. It's likely to be destination error, so requeue message[{}]", messageDelivery, e);
            CommonUtil.doThreadSleep(1000L);
        }
        // 네트워크 관련 예외 발생 시 해당 예외 처리 블럭으로 들어온다.
        catch (org.springframework.web.client.ResourceAccessException e) {
            if (e.getCause() instanceof TimeoutException | e.getCause() instanceof SocketTimeoutException) {
                log.error("[SUBMIT] Fail to send MM7_submit.REQ. It's likely to be internal problem firewall or else, so requeue message[{}]", messageDelivery, e);
            } else if(e.getCause() instanceof java.net.ConnectException) {
                log.error("[SUBMIT] Fail to send MM7_submit.REQ. It's likely to connection problem to destination server[SKT]", e);
            } else {
                log.error("[SUBMIT] Fail to send MM7_submit.REQ. It's likely to network problem", e);
            }

            CommonUtil.doThreadSleep(500L);
        }
        // 처리하지 못한 예외가 발생할 경우 해당 예외 처리 블럭으로 들어온다.
        catch (Exception e) {
            log.error("[SUBMIT] Fail to send MM7_Submit.REQ, requeue message[{}]", messageDelivery, e);
        }

        inboundMessage.basicNack();
    }



    private HttpEntity<String> getSubmitHttpEntity(String soapMessageToString) {
        HttpHeaders headers = new HttpHeaders();
        String httpBoundary = soapUtil.getHttpBoundary(soapMessageToString);
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/related; boundary=\"" + httpBoundary + "\"; " +
                "type=\"text/xml\"; charset=\"EUC-KR\"; start=\"" + Constants.KTF_CONTENT_ID + "\"");
        return new HttpEntity<>(soapMessageToString, headers);
    }
}