package kr.co.seoultel.message.mt.mms.direct.ktf;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfEchoReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfEchoResMessage;
import kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultSenderConfig;
import kr.co.seoultel.message.mt.mms.core_module.dto.endpoint.Endpoint;
import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.modules.HeartBeatClient;
import kr.co.seoultel.message.mt.mms.direct.util.ktf.KtfMMSReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;
import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.SECOND;


@Slf4j
@Component
@Conditional(KtfCondition.class)
public class KtfScheduler extends kr.co.seoultel.message.mt.mms.core_module.modules.MMSScheduler {

    protected final Endpoint endpoint;

    protected final RestTemplate restTemplate = new RestTemplate();
    protected final KtfMMSReportUtil ktfMMSReportUtil = new KtfMMSReportUtil();


    public KtfScheduler(@Value("${sender.http.endpoint.ip}") String ip, @Value("${sender.http.endpoint.port}") int port, HeartBeatClient heartBeatClient,
                        MultiMediaService fileService, HashMapStorage<String, String> fileStorage, QueueStorage<MrReport> reportQueueStorage, HashMapStorage<String, MessageHistory> historyStorage, HashMapStorage<String, MessageDelivery> deliveryStorage) {

        super(new KtfEndpoint(ip, port), fileService, heartBeatClient, fileStorage, reportQueueStorage, historyStorage, deliveryStorage);
        this.endpoint = new KtfEndpoint(ip, port);
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
                    // 내부 만료 처리를 위한 리포트 메세지 생성
                    KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = KtfDeliveryReportReqMessage.builder()
                                                                                                         .tid("")
                                                                                                         .messageId(messageId)
                                                                                                         .callback(messageDelivery.getCallback())
                                                                                                         .receiver(messageDelivery.getReceiver())
                                                                                                         .mmStatus(Constants.MASSAGE_IS_EXPIRED_MNO_RESULT)
                                                                                                         .build();

                    ktfMMSReportUtil.prepareToReport(messageDelivery, ktfDeliveryReportReqMessage);

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
}
