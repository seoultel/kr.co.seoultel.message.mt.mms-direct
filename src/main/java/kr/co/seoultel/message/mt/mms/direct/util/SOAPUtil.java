package kr.co.seoultel.message.mt.mms.direct.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.xml.soap.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

public abstract class SOAPUtil {

    protected final HttpClientProperty property;
    public SOAPUtil(HttpClientProperty property) {
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

    protected abstract String createSOAPMessage(MessageDelivery messageDelivery) throws SOAPException, IOException, MessagingException;




    public static MimeBodyPart createTextMimeMultipart(MimeMultipart mimeMultipart, String message, List<String> imageNames) throws MessagingException {
        MimeBodyPart textMimeBodyPart = new MimeBodyPart();

        /* create HtmlTag of message and images */
        StringBuilder sb = new StringBuilder("<HTML><HEAD></HEAD></HTML>");
        for (String imageName : imageNames) {
            sb.append(String.format("<IMG SRC=\"%s\">", imageName));
        }

        sb.append(new String(message.getBytes(Charset.forName(EUC_KR))));
        sb.append("</BODY></HTML>");

        String htmlTag = sb.toString();

        /* set content  */
        textMimeBodyPart.setContent(htmlTag, "text/plain;charset=euc-kr");

        /* set header to MimeBodyPart */
        textMimeBodyPart.setHeader("Content-Type", "text/html; charset=euc-kr");
        textMimeBodyPart.setHeader("Content-ID", "<text/html>");
        textMimeBodyPart.setHeader("Content-Transfer-Encoding", "8bit");

        return textMimeBodyPart;
    }


//    private static void addHeaderToImageMimeBodyPart(MimeBodyPart mimeBodyPart) throws MessagingException {
//        mimeBodyPart.setHeader("Content-Type", "image/jpeg");
//        mimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");
//        mimeBodyPart.setHeader("Content-ID", "<arreo_jpeg.jpeg>");
//        mimeBodyPart.setHeader("Content-Disposition", "attachment; filename=\"arreo_jpeg.jpeg\"");
//    }

    public static SOAPMessage convertSOAPMessageToString(String soapMessageStr) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        return factory.createMessage(null, new ByteArrayInputStream(soapMessageStr.getBytes()));
    }
}
