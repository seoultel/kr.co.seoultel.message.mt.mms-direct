package kr.co.seoultel.message.mt.mms.direct.modules.client.http.skt.util;

import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.soap.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSubmitReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.DateUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core.util.ValidateUtil;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.util.SOAPUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
public class SktSoapUtil extends SOAPUtil {

    public SktSoapUtil(HttpClientProperty property) {
        super(property);
    }

    @Override
    public String createSOAPMessage(MessageDelivery messageDelivery) throws SOAPException, IOException, MessagingException {
        /*  Declare variables, use for render soap message  */
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

        boolean hasImage = !imageIds.isEmpty();
        boolean hasMessage = !ValidateUtil.isNullOrBlankStr(message);

        SktSubmitReqMessage sktSoapMessage = SktSubmitReqMessage.builder()
                                                                .tid("tid")
                                                                .vasId(property.getVasId())
                                                                .vaspId(property.getVaspId())
                                                                .cpid(property.getCpidInfo().getCpid())
                                                                .callback(callback)
                                                                .receiver(receiver)
                                                                .subject(subject)
                                                                .message(message)
                                                                .resellerCode(originCode)
                                                                .build();

        /*  Create MimeParts  */
        byte[] bytes;
        MimeMultipart mimeMultipart = new MimeMultipart("related");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//        SOAPMessage soapMessage = sktSoapMessage.toSOAPMessage();
//        if (hasImage) {
//            bytes = message.getBytes(Charset.forName(EUC_KR));
//        } else {
//            MimeBodyPart textMimeMultipart = SktSoapUtil.createTextMimeMultipart(property, message);
//
//            bytes = message.getBytes(Charset.forName(EUC_KR));
//        }
//
//        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, mimeMultipart.getContentType());
//        DataHandler dataHandler = new DataHandler(byteArrayDataSource);
//
//        AttachmentPart attachmentPart = soapMessage.createAttachmentPart(dataHandler);
//        soapMessage.addAttachmentPart(attachmentPart);
//        soapMessage.saveChanges();
//
//        baos.reset();
//        soapMessage.writeTo(baos);
//
//        return baos.toString();
        return null;
    }

//    /* TODO : \r\n 무조건 써야하는가 ? */
//    public static MimeBodyPart createTextMimeMultipart(HttpClientProperty property, String message) throws MessagingException {
//        MimeBodyPart textMimeBodyPart = new MimeBodyPart();
//
//        /* set header elements */
//        textMimeBodyPart.setHeader("Content-ID", "<" + conentId + ">");
//        textMimeBodyPart.setHeader("Content-Length", String.valueOf(contentLength));
//        textMimeBodyPart.setHeader("Content-Disposition", "attachment; filename=\"arreo_xt.xt\"");
//        textMimeBodyPart.setHeader("X-SKT-Content-Usage", "0");
//
//        /*
//         * TODO : "X-SKT-CIDSID" 어떠한 값을 넣을지 애매모호함.
//         *  우선 Legacy MMS G/W와 동일한 값을 사용;
//         */
//        textMimeBodyPart.setHeader("X-SKT-CIDSID", DateUtil.getDate(0, "yMMddHHmmss"));
//        textMimeBodyPart.setHeader("X-SKT-Service-Type", "0");
//
//        /* set body elements */
//        StringBuilder sb = new StringBuilder();
//        /* start xt */
//        sb.append("<?xml version=\"1.0\" encoding=\"euc-kr\"?>");
//        sb.append("<xt xmlns=\"http://www.w3.org/1999/xhtml\">");
//
//        /* create body */
//        sb.append("<body bgcolor=\"#FFFFFF\">");
//        sb.append(String.format("<div><color1>%s</color1></div>", message));
//        sb.append("</body>");
//        /* end body */
//
//        /* end xt */
//        sb.append("</xt>");
//
//        String body = sb.toString();
//
//        /* set content to textMimeBodypart */
//        textMimeBodyPart.setContent(body, "text/plain; charset=euc-kr");
//        return textMimeBodyPart;
//    }
}
