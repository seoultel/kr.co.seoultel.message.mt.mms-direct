package kr.co.seoultel.message.mt.mms.direct.util;

import jakarta.xml.soap.*;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

public abstract class SoapUtil {

    protected final HttpClientProperty property;
    protected final HashMapStorage<String, String> fileStorage;

    public SoapUtil(HttpClientProperty property, HashMapStorage<String, String> fileStorage) {
        this.property = property;
        this.fileStorage = fileStorage;
    }

    protected static MessageFactory messageFactory;
    protected static DocumentBuilderFactory documentBuilderFactory;

    static {
        try {
            
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            messageFactory = MessageFactory.newInstance();
        } catch (SOAPException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String createSOAPMessage(InboundMessage inboundMessage) throws MCMPSoapRenderException;


    public static String getSOAPMessageMM7LocalPart(String xml) throws MCMPSoapRenderException {
        try {
            int startBodyIndex = xml.indexOf("<env:Body>");
            int endBodyIndex = xml.indexOf("</env:Body>");

            String body = xml.substring(startBodyIndex, endBodyIndex);
            String localPart = body.substring(body.indexOf("<mm7:") + 5, body.indexOf(" xmlns"));

            return localPart;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public String getHttpBoundary(String soapMessageToString) {
        return soapMessageToString.substring(2, soapMessageToString.indexOf("\r\n"));
    }

    public static SOAPMessage convertSOAPMessageToString(String soapMessageStr) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        return factory.createMessage(null, new ByteArrayInputStream(soapMessageStr.getBytes()));
    }
}
