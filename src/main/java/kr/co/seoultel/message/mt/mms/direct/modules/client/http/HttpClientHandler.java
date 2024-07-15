package kr.co.seoultel.message.mt.mms.direct.modules.client.http;

import jakarta.mail.MessagingException;
import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.TpsOverExeption;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapCreateException;
import kr.co.seoultel.message.mt.mms.core.util.CommonUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.modules.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public abstract class HttpClientHandler {

    protected final HttpClientProperty property;

    protected final PersistenceManager persistenceManager;
    protected final ConcurrentLinkedQueue<MrReport> reportQueue;


    public HttpClientHandler(HttpClientProperty property, PersistenceManager persistenceManager, ConcurrentLinkedQueue<MrReport> reportQueue) {
        this.property = property;

        this.persistenceManager = persistenceManager;

        this.reportQueue = reportQueue;
    }

    protected void isTpsOver() throws TpsOverExeption {
        CommonUtil.isTpsOver(property.getRateLimiter());
    }


    protected abstract void doSubmit(InboundMessage inboundMessage) throws Exception;


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
