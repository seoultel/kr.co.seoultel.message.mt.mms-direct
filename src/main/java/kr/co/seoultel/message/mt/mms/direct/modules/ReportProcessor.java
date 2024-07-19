package kr.co.seoultel.message.mt.mms.direct.modules;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.util.CommonUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.MsgReportException;

import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReportProcessor;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReportService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class ReportProcessor extends MrReportProcessor {

    public ReportProcessor(MrReportService reportService, HashMapStorage<String, MessageDelivery> deliveryStorage, QueueStorage<MrReport> reportQueueStorage) {
        super(reportService, deliveryStorage, reportQueueStorage);
    }

    @Override
    public void run() {
        while (Application.isStarted()) {
            while (!reportQueueStorage.isEmpty()) {
                MrReport report = reportQueueStorage.remove();

                MessageDelivery messageDelivery = report.getMessageDelivery();
                String dstMsgId = messageDelivery.getDstMsgId();

                try {
                    reportService.sendReport(report);
                    log.info("[SYSTEM] Successfully send report of message[dstMsgId : {} & reportType : {} & state : {}]",
                            dstMsgId, report.getType(), DeliveryType.getDeliveryTypeEng(report.getType()));

                    if (report.getType().equals(DeliveryType.REPORT)) {
                        deliveryStorage.remove(dstMsgId);
                    }
                } catch (MsgReportException e) {
                    if (e.getOriginException() instanceof java.net.ConnectException) {
                        break;
                    }

                    log.error("[SYSTEM] Failed to send report of message[dstMsgId : {} & reportType : {} & state : {}], cause[{}]",
                            dstMsgId, report.getType(), DeliveryType.getDeliveryTypeEng(report.getType()), e.getMessage(), e);
                } catch (Exception e) {
                    log.error("[SYSTEM] Failed to send report of message[dstMsgId : {} & reportType : {} & state : {}], cause[{}]",
                            dstMsgId, report.getType(), DeliveryType.getDeliveryTypeEng(report.getType()), e.getMessage(), e);
                }
            }

            CommonUtil.doThreadSleep(Constants.SECOND);
        }
    }
}
