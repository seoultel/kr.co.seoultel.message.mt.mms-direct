package kr.co.seoultel.message.mt.mms.direct.modules.client.http.skt;

import jakarta.mail.MessagingException;
import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientHandler;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.skt.util.SktSoapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class SktClientHandler extends HttpClientHandler {
    protected final SktEndpoint endpoint;
    protected final SktSoapUtil soapUtil;

    public SktClientHandler(HttpClientProperty property, PersistenceManager persistenceManager, ConcurrentLinkedQueue<MrReport> reportQueue) {
        super(property, persistenceManager, reportQueue);

        this.soapUtil = new SktSoapUtil(property);
        this.endpoint = new SktEndpoint(property);
    }


    @Override
    protected void doSubmit(InboundMessage inboundMessage) throws IOException, NAckException, MessagingException, SOAPException {
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();
        soapUtil.createSOAPMessage(messageDelivery);

//        log.info("soapMessageToString : {}", soapMessageToString);


//        /* Request of submit */
//        HttpEntity<String> httpEntity = getSubmitHttpEntity(soapMessageToString);
//        ResponseEntity<String> response = restTemplate.exchange(endpoint.getHttpUrl(),
//                HttpMethod.POST,
//                httpEntity,
//                String.class);
    }


}