package kr.co.seoultel.message.mt.mms.direct.skt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultSenderConfig;
import kr.co.seoultel.message.mt.mms.core_module.modules.heartBeat.HeartBeatProtocol;
import kr.co.seoultel.message.mt.mms.core_module.modules.heartBeat.client.DefaultHeartBeatClient;
import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.modules.HeartBeatClient;
import kr.co.seoultel.message.mt.mms.direct.util.skt.SktMMSReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collection;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.SECOND;


@Slf4j
@Component
@Conditional(SktCondition.class)
public class SktScheduler extends kr.co.seoultel.message.mt.mms.core_module.modules.MMSScheduler {

    protected final SktMMSReportUtil sktMMSReportUtil = new SktMMSReportUtil();
    public SktScheduler(@Value("${sender.http.endpoint.ip}") String ip, @Value("${sender.http.endpoint.port}") int port, HeartBeatClient heartBeatClient,
                        MultiMediaService fileService, HashMapStorage<String, String> fileStorage, QueueStorage<MrReport> reportQueueStorage, HashMapStorage<String, MessageHistory> historyStorage, HashMapStorage<String, MessageDelivery> deliveryStorage) {
        super(new SktEndpoint(ip, port), fileService, heartBeatClient, fileStorage, reportQueueStorage, historyStorage, deliveryStorage);
    }

    @Scheduled(fixedDelay = 30000L)
    public void processExpiredMessages() {
        Collection<MessageHistory> histories = historyStorage.snapshot();

        // 만료된 메세지만 가져오기
        histories.stream().filter(MessageHistory::isExpire).forEach((history) -> {
            String messageId = history.getMessageId();

            // 해당 메세지의 MessageDelivery 찾을 수 있는 경우
            if (deliveryStorage.containsKey(messageId)) {
                MessageDelivery messageDelivery = deliveryStorage.get(messageId);

                try {
                    SktDeliveryReportReqMessage sktDeliveryReportReqMessage = SktDeliveryReportReqMessage.builder()
                                                                                                         .tid("")
                                                                                                         .messageId(messageId)
                                                                                                         .senderAddress(messageDelivery.getCallback())
                                                                                                         .receiver(messageDelivery.getReceiver())
                                                                                                         .statusCode(Constants.MASSAGE_IS_EXPIRED_MNO_RESULT)
                                                                                                         .statusText("EXPIRED_MSG")
                                                                                                         .build();

                    sktMMSReportUtil.prepareToReport(messageDelivery, sktDeliveryReportReqMessage);

                    MrReport mrReport = new MrReport(DeliveryType.REPORT, messageDelivery);
                    reportQueueStorage.add(mrReport);
                    log.info("[EXPIRED] Expired message[{}] pushed in report-queue", messageId);
                } catch (MCMPSoapRenderException e) {
                    log.error("[EXPIRED] Fail to create expired SktDeliveryReportReqMessage by message[{}]", messageDelivery);
                }
            } else {
                // 해당 메세지의 MessageDelivery 찾을 수 없는 경우
                historyStorage.remove(messageId);
                log.error("[EXPIRED] Expired message[{}], delivery-storage hasn't that, is removed in history-storage", messageId);
            }
        });
    }

    /*
     * TODO : PING 에 대한 정상 응답을 BadRequest 로 해도 괜찮은지 여부;
     */
    @Override
    @Scheduled(fixedDelay = 30 * SECOND)
    protected void ping() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("[PING] Try to send ping to {}[{}]", DefaultSenderConfig.TELECOM, endpoint.getHttpUrl());
            ResponseEntity<String> response = restTemplate.exchange(endpoint.getDefaultUrl(), HttpMethod.GET, entity, String.class);
        } catch (HttpClientErrorException.BadRequest e) {
            String message = e.getMessage();
            if ("400 Bad Request: [no body]".equals(message)) {
                isConnected = true;
                heartBeatClient.setHStatus(HeartBeatProtocol.HEART_SUCCESS);
                log.info("[PONG] Successfully received pong[{}] to {}[{}]", "\"CONNECTED\"", DefaultSenderConfig.TELECOM, endpoint.getDefaultUrl());
            }
        } catch (Exception e) {
            this.isConnected = false;
            heartBeatClient.setHStatus(HeartBeatProtocol.DST_CONNECTION_ERROR);
            log.error("[PING] Fail to send ping or received pong[\"DISCONNECTED\"] to [{}:{}]", DefaultSenderConfig.TELECOM, endpoint.getHttpUrl(), e);
        }
    }
}
