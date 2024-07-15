package kr.co.seoultel.message.mt.mms.direct.util;

import jakarta.xml.soap.*;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class SoapUtil {

    protected final HttpClientProperty property;
    public SoapUtil(HttpClientProperty property) {
        this.property = property;
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

    protected abstract String createSOAPMessage(InboundMessage inboundMessage) throws Exception;


    public static String getSOAPMessageMM7LocalPart(String xml) throws SOAPException, IOException {
        int startBodyIndex = xml.indexOf("<env:Body>");
        int endBodyIndex = xml.indexOf("</env:Body>");

        String body = xml.substring(startBodyIndex, endBodyIndex);
        String localPart = body.substring(body.indexOf("<mm7:") + 5, body.indexOf(" xmlns"));

        return localPart;
    }

    public String getHttpBoundary(String soapMessageToString) {
        return soapMessageToString.substring(2, soapMessageToString.indexOf("\r\n"));
    }

    public static SOAPMessage convertSOAPMessageToString(String soapMessageStr) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        return factory.createMessage(null, new ByteArrayInputStream(soapMessageStr.getBytes()));
    }
}
