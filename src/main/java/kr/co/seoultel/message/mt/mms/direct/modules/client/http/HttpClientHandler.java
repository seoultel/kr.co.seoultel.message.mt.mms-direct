package kr.co.seoultel.message.mt.mms.direct.modules.client.http;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.TpsOverExeption;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.util.CommonUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;

import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class HttpClientHandler {

    protected final HttpClientProperty property;

    protected final HashMapStorage<String, MessageDelivery> deliveryStorage;
    protected final QueueStorage<MrReport> reportQueueStorage;


    public HttpClientHandler(HttpClientProperty property, HashMapStorage<String, MessageDelivery> deliveryStorage, QueueStorage<MrReport> reportQueueStorage) {
        this.property = property;

        this.deliveryStorage = deliveryStorage;
        this.reportQueueStorage = reportQueueStorage;
    }

    protected void isTpsOver() throws TpsOverExeption {
        CommonUtil.isTpsOver(property.getRateLimiter());
    }


    public void addReportQueue(MrReport mrReport) {
        reportQueueStorage.add(mrReport);
    }

    protected abstract void doSubmit(InboundMessage inboundMessage) throws MCMPSoapRenderException, NAckException;


    public String getBpid() {
        return property.getBpid();
    }

    public String getVasId() {
        return property.getVasId();
    }

    public String getVaspId() {
        return property.getVaspId();
    }

}
