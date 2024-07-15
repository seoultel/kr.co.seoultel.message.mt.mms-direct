package kr.co.seoultel.message.mt.mms.direct.skt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.protocol.SktProtocol;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSubmitResMessage;
import kr.co.seoultel.message.mt.mms.core.util.CommonUtil;
import kr.co.seoultel.message.mt.mms.core.util.DateUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientHandler;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.util.skt.SktMMSReportUtil;
import kr.co.seoultel.message.mt.mms.direct.util.skt.SktSoapUtil;
import kr.co.seoultel.message.mt.mms.direct.util.skt.SktUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

@Slf4j
public class SktClientHandler extends HttpClientHandler {
    protected final RestTemplate restTemplate = new RestTemplateBuilder().build();


    protected final SktEndpoint endpoint;

    protected final SktSoapUtil soapUtil;
    protected final SktMMSReportUtil sktMMSReportUtil = new SktMMSReportUtil();


    public SktClientHandler(HttpClientProperty property, PersistenceManager persistenceManager, ConcurrentLinkedQueue<MrReport> reportQueue) {
        super(property, persistenceManager, reportQueue);

        this.soapUtil = new SktSoapUtil(property);
        this.endpoint = new SktEndpoint(property);
    }

    /*
     * TODO : 이통사로 Http 요청을 보내는 경우 예외 발생 시 NACK 전송을 위해서 return; 이 들어간 부분이 있음.
     *        예외 처리를 해당 메서드 내부에서 잡아야 하는 이유가 있는지.
     *        Aspect 사용하여 예외 처리할 때 발생하는 문제가 뭐가 있을지 검토.
     *
     */
    @Override
    protected void doSubmit(InboundMessage inboundMessage) throws Exception {
        String soapMessageToString = soapUtil.createSOAPMessage(inboundMessage);
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();

        String umsMsgId = messageDelivery.getUmsMsgId();

        sktMMSReportUtil.prepareToSubmit(FallbackUtil.isFallback(messageDelivery), messageDelivery);

        try {
            /* Request of submit */
            HttpEntity<String> httpEntity = getSubmitHttpEntity(soapMessageToString);
            ResponseEntity<String> response = restTemplate.exchange(endpoint.getHttpUrl(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class);


            String xml = response.getBody();
            assert xml != null;

            SktSubmitResMessage sktSubmitResMessage = new SktSubmitResMessage();
            sktSubmitResMessage.fromXml(xml);

            String messageId = sktSubmitResMessage.getMessageId();
            String statusCode = sktSubmitResMessage.getStatusCode();
            String statusText = sktSubmitResMessage.getStatusText();
            messageDelivery.setDstMsgId(messageId);
//
            NAckType nAckType = SktUtil.getNAckTypeSubmitAckStatusCode(statusCode);
            if (nAckType.equals(NAckType.ACK)) {
                MessageDelivery cloneDelivery = (MessageDelivery) messageDelivery.clone();

                switch (statusCode) {
                    case SktProtocol.SUCCESS:
                        log.info("[SUBMIT_ACK | SUCCESS] Successfully received SubmitAck of message[{}] from SKT", cloneDelivery);
                        sktMMSReportUtil.prepareToSubmitAck(messageDelivery, sktSubmitResMessage);
                        persistenceManager.saveMessageByUmsMsgId(messageId, cloneDelivery);
                        break;

                    default:
                        log.info("[SUBMIT_ACK & FAIL] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from SKT", sktSubmitResMessage, umsMsgId);
                        break;
                }

                MrReport mrReport = new MrReport(DeliveryType.SUBMIT_ACK, cloneDelivery);
                /*reportQueueStorage.*/
                reportQueue.add(mrReport);
                log.info("[SUBMIT-ACK] Successfully add Report[{}] in reportQueue", sktSubmitResMessage);

                inboundMessage.basicAck();
                return;
            } else {
                /* 전송 가능 TPS를 초과한 경우 */
                if (sktSubmitResMessage.isTpsOver()) {
                    log.warn("[SUBMIT_ACK & TPS OVER] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from SKT", sktSubmitResMessage, umsMsgId);
                    CommonUtil.doThreadSleep(DateUtil.getTimeGapUntilNextSecond());
                } else if (sktSubmitResMessage.isHubspError()) {
                    log.error("[SUBMIT_ACK & HUBSP] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from SKT", sktSubmitResMessage, umsMsgId);
                    CommonUtil.doThreadSleep(Constants.SECOND);
                } else {
                    log.warn("[SUBMIT_ACK | FAIL] Successfully received SubmitAck[{}] of message[umsMsgId : {}] from SKT", sktSubmitResMessage, umsMsgId);
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
        int startCidIndex = soapMessageToString.indexOf("Content-ID:") + 13;
        String startCid = soapMessageToString.substring(startCidIndex + 1, startCidIndex + 21);

        String httpBoundary = soapMessageToString.substring(2, soapMessageToString.indexOf("\r\n"));
        headers.add(HttpHeaders.CONTENT_TYPE, "multipart/related; " +
                "boundary=\"" + httpBoundary + "\"; " +
                "type=\"text/xml\"; charset=\"EUC-KR\"; start=\"<start_MM7_SOAP>\"");
        headers.add("accept-encoding", "gzip");
        headers.add("SOAPAction", Constants.SOAP_ACTION);
        headers.add("accept", "*/*");
        headers.add("Accept-Charset", "euc-kr");
        headers.add(HttpHeaders.USER_AGENT, "");

        return new HttpEntity<>(soapMessageToString, headers);
    }
}