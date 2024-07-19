package kr.co.seoultel.message.mt.mms.direct.lgt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryState;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.ExpirerService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.ktf.KtfCondition;
import kr.co.seoultel.message.mt.mms.direct.util.lgt.LgtMMSReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;


@Slf4j
@Component
@Conditional(LgtCondition.class)
public class LgtScheduler extends kr.co.seoultel.message.mt.mms.core_module.modules.MMSScheduler {

    protected final LgtMMSReportUtil lgtMMSReportUtil = new LgtMMSReportUtil();
    public LgtScheduler(ExpirerService expirerService, QueueStorage<MrReport> reportQueueStorage, HashMapStorage<String, MessageHistory> historyStorage, HashMapStorage<String, MessageDelivery> deliveryStorage) {
        super(expirerService, reportQueueStorage, historyStorage, deliveryStorage);
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
                    LgtDeliveryReportReqMessage lgtDeliveryReportReqMessage = LgtDeliveryReportReqMessage.builder()
                                                                                                         .tid("")
                                                                                                         .messageId(messageId)
                                                                                                         .callback(messageDelivery.getCallback())
                                                                                                         .receiver(messageDelivery.getReceiver())
                                                                                                         .mmStatus(Constants.MASSAGE_IS_EXPIRED_MNO_RESULT)
                                                                                                         .build();

                    lgtMMSReportUtil.prepareToReport(messageDelivery, lgtDeliveryReportReqMessage);

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
