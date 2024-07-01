package kr.co.seoultel.message.mt.mms.direct.modules.client.http.lgt.util;

import jakarta.xml.soap.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.util.SOAPUtil;

import javax.xml.namespace.QName;
import java.util.List;

public class LgtSoapUtil extends SOAPUtil {

    public LgtSoapUtil(HttpClientProperty property) {
        super(property);
    }

    @Override
    protected String createSOAPMessage(MessageDelivery messageDelivery) throws SOAPException {
        boolean isFallback = FallbackUtil.isFallback(messageDelivery);
        String umsMsgId = messageDelivery.getUmsMsgId();
        String dstMsgId = FallbackUtil.getDstMsgId(messageDelivery);
        String groupCode = messageDelivery.getGroupCode();
        String receiver = messageDelivery.getReceiver();
        String callback = messageDelivery.getCallback();
        String subject = FallbackUtil.getSubject(messageDelivery);
        String message = FallbackUtil.getMessage(messageDelivery);
        String originCode = FallbackUtil.getOriginCode(messageDelivery);
        List<String> imageIds = FallbackUtil.getMediaFiles(messageDelivery);

        SOAPMessage soapMessage = messageFactory.createMessage();
        soapMessage.setProperty(SOAPMessage.WRITE_XML_DECLARATION, "true");
        soapMessage.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "euc-kr");

        /* SOAP Part */
        SOAPPart soapPart = soapMessage.getSOAPPart();
        soapPart.setContentId(Constants.LGT_CONTENT_ID);

        /* SOAP Envelope */
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.removeNamespaceDeclaration("SOAP-ENV");
        envelope.setPrefix("env");

        /* SOAP Header */
        SOAPHeader soapHeader = envelope.getHeader();
        soapHeader.setPrefix("env");
        soapHeader.addHeaderElement(new QName(Constants.LGT_TRANSACTION_ID_URL, "TransactionID", "mm7"))
                .addTextNode(dstMsgId).setAttribute("env:mustUnderstand", "1");

        /* SOAP Body */
        SOAPBody soapBody = envelope.getBody();
        soapBody.setPrefix("env");

        return null;
    }
}
