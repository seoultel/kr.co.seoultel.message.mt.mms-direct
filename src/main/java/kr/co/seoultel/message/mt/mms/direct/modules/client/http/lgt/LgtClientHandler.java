package kr.co.seoultel.message.mt.mms.direct.modules.client.http.lgt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientHandler;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.lgt.util.LgtSoapUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class LgtClientHandler extends HttpClientHandler {

    protected final LgtEndpoint endpoint;
    protected final LgtSoapUtil soapUtil;
    public LgtClientHandler(HttpClientProperty property, PersistenceManager persistenceManager, ConcurrentLinkedQueue<MrReport> reportQueue) {
        super(property, persistenceManager, reportQueue);

        this.soapUtil = new LgtSoapUtil(property);
        this.endpoint = new LgtEndpoint(property);
    }

    @Override
    protected void doSubmit(InboundMessage inboundMessage) throws FileServerException, IOException, NAckException {
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();
        Map<String, Object> content = messageDelivery.getContent();

        // Varaibles
        boolean isFallback = FallbackUtil.isFallback(messageDelivery);
        String umsMsgId = messageDelivery.getUmsMsgId();
        String groupCode = messageDelivery.getGroupCode();
        String receiver = messageDelivery.getReceiver();
        String callback = messageDelivery.getCallback();
        String subject = FallbackUtil.getSubject(messageDelivery);
        String message = FallbackUtil.getMessage(messageDelivery);
        String originCode = FallbackUtil.getOriginCode(messageDelivery);
        List<String> imageIds = FallbackUtil.getMediaFiles(messageDelivery);
//        try {
//            log.info("MessageDelivery : {}", inboundMessage);
//
//            inboundMessage.basicAck();
//        } catch (NAckException e) {
//            log.error("??");
//        }
    }
}
