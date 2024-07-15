package kr.co.seoultel.message.mt.mms.direct.util.ktf;

import jakarta.activation.DataHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.soap.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.nio.charset.Charset;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfSubmitReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.ValidateUtil;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.util.SoapUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;
import lombok.extern.slf4j.Slf4j;


/*
 *
 */
@Slf4j
public class KtfSoapUtil extends SoapUtil {

    public KtfSoapUtil(HttpClientProperty property) {
        super(property);
    }


    @Override
    public String createSOAPMessage(InboundMessage inboundMessage) throws Exception {
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();

        /*  Declare variables, use for render soap message  */
        String groupCode = messageDelivery.getGroupCode();

        String umsMsgId = messageDelivery.getUmsMsgId();
        String receiver = messageDelivery.getReceiver();
        String callback = messageDelivery.getCallback();
        String message = FallbackUtil.getMessage(messageDelivery);
        String subject = FallbackUtil.getSubject(messageDelivery);
        String originCode = FallbackUtil.getOriginCode(messageDelivery);
        List<String> imageIds = FallbackUtil.getMediaFiles(messageDelivery);

        boolean hasImage = !imageIds.isEmpty();
        boolean hasMessage = !ValidateUtil.isNullOrBlankStr(message);

        KtfSubmitReqMessage ktfSoapMessage = KtfSubmitReqMessage.builder()
                .tid(KtfUtil.getRandomTransactionalId(umsMsgId))
                .vasId(property.getVasId())
                .vaspId(property.getVaspId())
                .cpid(property.getCpidInfo().getCpid())
                .callback(callback)
                .receiver(receiver)
                .subject(subject)
                .resellerCode(originCode)
                .build();


        /*  Create MimeParts  */
        byte[] bytes;
        MimeMultipart mimeMultipart = new MimeMultipart("related");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SOAPMessage soapMessage = ktfSoapMessage.toSOAPMessage();
        if (hasImage) {
            MimeBodyPart textMimeBodyPart = createTextMimeMultipart(mimeMultipart, message, imageIds);
            mimeMultipart.addBodyPart(textMimeBodyPart);

            for (String imageId : imageIds) {
//                String imagePath = ImageService.getImages().get(ImageUtil.getImageKey(groupCode, imageId));
//                byte[] imageBytes = ImageUtil.getImageBytes(imagePath);
//                int mediaFileSize = imageBytes.length;
//                if (mediaFileSize > Constants.STANDARD_IMAGE_MAX_SIZE) {
//                    throw new AttachedImageSizeOverException(inboundMessage);
//                }

                // TODO : 지워야됨;
                byte[] imageBytes = new byte[]{1,0,0,1,0,1};

                // String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                MimeBodyPart imageMimeBodyPart = KtfSoapUtil.createImageMimeMultipart(imageBytes);
                mimeMultipart.addBodyPart(imageMimeBodyPart);
            }

            mimeMultipart.writeTo(baos);
            bytes = baos.toString(StandardCharsets.UTF_8).getBytes(Charset.forName(EUC_KR));
        } else {
            bytes = message.getBytes(Charset.forName(EUC_KR));
        }

        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, mimeMultipart.getContentType());
        DataHandler dataHandler = new DataHandler(byteArrayDataSource);

        AttachmentPart attachmentPart = soapMessage.createAttachmentPart(dataHandler);
        if (!hasImage) {
            attachmentPart.setMimeHeader("Content-Type", "text/plain; charset=\"euc-kr\"");
            attachmentPart.setMimeHeader("Content-ID", "<text/plain>");
            attachmentPart.setMimeHeader("Content-Transfer-Encoding", "8bit");
        }

        soapMessage.addAttachmentPart(attachmentPart);
        soapMessage.saveChanges();

        baos.reset();
        soapMessage.writeTo(baos);

        return baos.toString();
    }


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

    private static MimeBodyPart createImageMimeMultipart(byte[] imageBytes) throws MessagingException {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();

        /* set header to MimeBodyPart */
        mimeBodyPart.setHeader("Content-ID", "<arreo_jpeg.jpeg>");
        mimeBodyPart.setHeader("Content-Type", "image/jpeg");
        mimeBodyPart.setHeader("Content-Disposition", "attachment; filename=\"arreo_jpeg.jpeg\"");
        mimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");

        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(imageBytes, "image/jpeg");
        mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        return mimeBodyPart;
    }



    public static String setContentTypeToSoapMessage(String soapMessage) {
        String[] lines = soapMessage.split("\\r?\\n");
        String textContentType = "Content-Type: text/html; charset=\"euc-kr\"";
        String contentEncoding = "Content-Transfer-Encoding: 8bit";
        String contentId = "Content-ID: <SaturnPics-01020930@news.tnn.com>";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("Content-Type: multipart/related;")) {
                log.info("분기 1 :: (lines[i].contains(\"Content-Type: multipart/related;\"))");
                lines[i + 1] += "\r\n" + contentId;
            }
            if (lines[i].contains("Content-ID: <text/html>")) {
                log.info("분기 2 :: (lines[i].contains(\"Content-ID: <text/html>\"))");
                lines[i] += "\r\n" + textContentType
                        + "\r\n" + contentEncoding;
            }
            result.append(lines[i]).append("\r\n");
        }

        return result.toString();
    }

}
