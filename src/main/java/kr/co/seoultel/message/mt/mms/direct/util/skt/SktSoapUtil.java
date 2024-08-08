package kr.co.seoultel.message.mt.mms.direct.util.skt;

import jakarta.activation.DataHandler;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.soap.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSubmitReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.*;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.AttachedImageSizeOverException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.distributor.AutoIncreaseNumberDistributor;
import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.ImageUtil;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.util.SoapUtil;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
public class SktSoapUtil extends SoapUtil {

    public SktSoapUtil(HttpClientProperty property, HashMapStorage<String, String> fileStorage) {
        super(property, fileStorage);
    }

    protected final AutoIncreaseNumberDistributor distributor = new AutoIncreaseNumberDistributor(5);

    @Override
    public String createSOAPMessage(InboundMessage inboundMessage) throws MCMPSoapRenderException {
        try {
            MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();

            long randomNumber = (long) (Math.random() * (10_000_000_000L - 1_000_000_000) + 1_000_000_000);   //10자리 난수

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


            // String startCid = getRandomContentID();
            String smilCID = getContentID(0, randomNumber);
            // List<String> imageCIDList = IntStream.range(0, 1 + imageIds.size()).mapToObj((idx) -> SktSoapUtil.getContentID(idx, randomNumber)).collect(Collectors.toUnmodifiableList());
            List<String> imageCIDList = IntStream.rangeClosed(0, 1 + imageIds.size()).mapToObj((idx) -> SktSoapUtil.getContentID(idx, randomNumber)).collect(Collectors.toUnmodifiableList());
            String messageCID = getContentID(imageIds.size() + 1, randomNumber);

            boolean hasImage = !imageIds.isEmpty();
            boolean hasMessage = !ValidateUtil.isNullOrBlankStr(message);

            SktSubmitReqMessage sktSoapMessage = SktSubmitReqMessage.builder()
                                                                    .tid(String.format("%05d", distributor.get()))
                                                                    .vasId(property.getVasId())
                                                                    .vaspId(property.getVaspId())
                                                                    .cpid(property.getCpidInfo().getCpid())
                                                                    .callback(callback)
                                                                    .receiver(receiver)
                                                                    .subject(subject)
                                                                    .message(message)
                                                                    .originCode(originCode)
                                                                    .smilCID(smilCID)
                                                                    .build();

            SOAPMessage soapMessage = sktSoapMessage.toSOAPMessage();

            /*  Create MimeMultipart And add MimeBodyPart(Smil, Xhtml) and AttachedPart  */
            MimeMultipart mimeMultipart = new MimeMultipart("related");

            /* create Smil Document and convert to String.class */
            String smilString = ConvertorUtil.convertDocumentToString(true, getSmilDocument(message, imageIds, messageCID, imageCIDList));

            /* convert SmilString to MimeBodyPart and add SmilMimeBodyPart to MimeMultipart */
            MimeBodyPart smilMimeBodyPart = getSmilMimeBodyPart(smilString, smilCID);
            mimeMultipart.addBodyPart(smilMimeBodyPart);

            /* create Xhtml Document and convert to String.class */
            String smilXhtmlString = ConvertorUtil.convertDocumentToString(true, getSmilXhtmlDocument(message, imageIds, messageCID, imageCIDList));

            /* convert XhtmlString to MimeBodyPart and add XhtmlMimeBodyPart to MimeMultipart */
            MimeBodyPart xmilXhtmlMimeBodyPart = getSmilXhtmlMimeBodyPart(smilXhtmlString, messageCID);
            mimeMultipart.addBodyPart(xmilXhtmlMimeBodyPart);

            for (String imageId : imageIds) {
                String imagePath = fileStorage.get(ImageUtil.getImageKey(groupCode, imageId));
                byte[] imageBytes = ImageUtil.getImageBytes(imagePath);
                int imageLength = imageBytes.length;

                /* TOOD : ENCODED IMAGE의 SIZE에 대한 검측이 있어야하는가 ? */
                if (imageLength > Constants.STANDARD_IMAGE_MAX_SIZE) {
                    throw new AttachedImageSizeOverException(inboundMessage);
                }
                
                String imageCID = imageCIDList.get(imageIds.indexOf(imageId));

                MimeBodyPart imageMimeBodyPart = new MimeBodyPart();

                ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(imageBytes, "image/jpeg");
                DataHandler dataHandler = new DataHandler(byteArrayDataSource);

                imageMimeBodyPart.setDataHandler(dataHandler);

                imageMimeBodyPart.setHeader("Content-ID", String.format("<%s>", imageCID));
                imageMimeBodyPart.setHeader("Content-Type", "image/jpeg");
                imageMimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");
                imageMimeBodyPart.setHeader("Content-Disposition", String.format("attachment; filename=\"%s.jpeg\"", imageId));
                imageMimeBodyPart.setHeader("X-SKT-Content-Usage", "0");
                imageMimeBodyPart.setHeader("X-SKT-CIDSID", getCIDSID());
                imageMimeBodyPart.setHeader("X-SKT-Service-Type", "0");


                mimeMultipart.addBodyPart(imageMimeBodyPart);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mimeMultipart.writeTo(baos);

            // baos 를 인코딩
            byte[] bytes = baos.toString(StandardCharsets.UTF_8).getBytes("euc-kr");

            ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, mimeMultipart.getContentType());
            DataHandler dataHandler = new DataHandler(byteArrayDataSource);

            // Attachment
            AttachmentPart attachmentPart = soapMessage.createAttachmentPart(dataHandler);
            soapMessage.addAttachmentPart(attachmentPart);
            soapMessage.saveChanges();

            // soapMessage to String
            baos.reset();
            soapMessage.writeTo(baos);
            baos.close();
            return baos.toString("euc-kr");
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public static MimeBodyPart getSmilMimeBodyPart(String smilToString, String smilCID) throws MCMPSoapRenderException {
        try {
            int length = smilToString.getBytes(EUC_KR).length;

            MimeBodyPart smilMimeBodyPart = new MimeBodyPart();
            smilMimeBodyPart.setContent(smilToString, "text/plain; charset=euc-kr");

            smilMimeBodyPart.setHeader("Content-ID", String.format("<%s>", smilCID));
            smilMimeBodyPart.setHeader("Content-Length", String.valueOf(length));
            smilMimeBodyPart.setHeader("Content-Type", "application/smil");

            smilMimeBodyPart.setHeader("Content-Disposition", "attachment; filename=\"arreo_smil.smi\"");
            smilMimeBodyPart.setHeader("X-SKT-Content-Usage", "0");
            smilMimeBodyPart.setHeader("X-SKT-CIDSID", getCIDSID());
            smilMimeBodyPart.setHeader("X-SKT-Service-Type", "0");


            return smilMimeBodyPart;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public static MimeBodyPart getSmilXhtmlMimeBodyPart(String smilXhtmlToString, String messageCID) throws MCMPSoapRenderException {
        try {
            int length = smilXhtmlToString.getBytes(EUC_KR).length;

            MimeBodyPart smilXhtmlMimeBodyPart = new MimeBodyPart();
            smilXhtmlMimeBodyPart.setContent(smilXhtmlToString, "text/plain; charset=euc-kr");

            smilXhtmlMimeBodyPart.setHeader("Content-ID", String.format("<%s>", messageCID));
            smilXhtmlMimeBodyPart.setHeader("Content-Length", String.valueOf(length));
            smilXhtmlMimeBodyPart.setHeader("Content-Type", "text/x-sktxt");
            smilXhtmlMimeBodyPart.setHeader("Content-Disposition", "attachment; filename=\"arreo_xhtml.xt\"");
            smilXhtmlMimeBodyPart.setHeader("X-SKT-Content-Usage", "0");
            smilXhtmlMimeBodyPart.setHeader("X-SKT-CIDSID", getCIDSID());
            smilXhtmlMimeBodyPart.setHeader("X-SKT-Service-Type", "0");


            return smilXhtmlMimeBodyPart;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public static Document getSmilDocument(String message, List<String> imageIds, String messageCID, List<String> imageCIDList) throws MCMPSoapRenderException {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.setXmlStandalone(true);

            /* <smil xmlns="http://www.w3.org/2001/SMIL20/Language" > */
            Element smil = document.createElement("smil");
            smil.setAttribute("xmlns", "http://www.w3.org/2001/SMIL20/Language");
            document.appendChild(smil);

            /* <head></head> */
            Element head = smil.getOwnerDocument().createElement("head");
            smil.appendChild(head);

            /* <layout></layout> */
            Element layout = head.getOwnerDocument().createElement("layout");
            layout.setAttribute("background-color", "#E7E3E7");
            head.appendChild(layout);

            /* <root-layout></root-layout> */
            Element rootLayout = layout.getOwnerDocument().createElement("root-layout");
            layout.appendChild(rootLayout);

            Element imageRegion = layout.getOwnerDocument().createElement("region");
            imageRegion.setAttribute("id", "image");
            imageRegion.setAttribute("top", "0%");
            imageRegion.setAttribute("left", "0%");
            imageRegion.setAttribute("width", "100%");
            imageRegion.setAttribute("height", "100%");
            imageRegion.setAttribute("z-index", "0");

            Element textRegion = layout.getOwnerDocument().createElement("region");
            textRegion.setAttribute("id", "text");
            textRegion.setAttribute("top", "0%");
            textRegion.setAttribute("left", "0%");
            textRegion.setAttribute("width", "100%");
            textRegion.setAttribute("height", "100%");
            textRegion.setAttribute("z-index", "0");

            rootLayout.appendChild(imageRegion);
            rootLayout.appendChild(textRegion);

            /* <body></body> */
            Element body = smil.getOwnerDocument().createElement("body");
            smil.appendChild(body);

            Element par = body.getOwnerDocument().createElement("par");
            par.setAttribute("repeatCount", "indefinite");
            body.appendChild(par);

            Element seq = body.getOwnerDocument().createElement("seq");
            seq.setAttribute("repeatCount", "indefinite");

            par.appendChild(seq);

            for (String imageId : imageIds) {
                String contentId = imageCIDList.get(imageIds.indexOf(imageId));

                Element image = par.getOwnerDocument().createElement("img");
                image.setAttribute("id", String.format("%s.jpeg", imageId));
                image.setAttribute("region", "image");
                image.setAttribute("src", contentId);

                seq.appendChild(image);
            }

            Element text = par.getOwnerDocument().createElement("text");
            text.setAttribute("repeatCount", "indefinite");
            text.setAttribute("region", "text");
            text.setAttribute("begin", "0");
            text.setAttribute("dur", "indefinite");
            text.setAttribute("src", messageCID);
            par.appendChild(text);

            Element textParam = text.getOwnerDocument().createElement("param");
            textParam.setAttribute("name", "style");
            textParam.setAttribute("value", "scroll");
            textParam.setAttribute("valuetype", "data");
            text.appendChild(textParam);

            return document;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }


    public static Document getSmilXhtmlDocument(String message, List<String> imageIds, String messageCID, List<String> imageCIDList) throws MCMPSoapRenderException {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            document.setXmlStandalone(true);

            Element xt = document.createElement("xt");
            xt.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");
            document.appendChild(xt);

            /* <head></head> */
            Element head = xt.getOwnerDocument().createElement("head");
            xt.appendChild(head);


            /* <body bgcolor="#FFFFFF"></body> */
            Element body = xt.getOwnerDocument().createElement("body");
            body.setAttribute("bgcolor", "#FFFFFF");
            xt.appendChild(body);

            Element div = body.getOwnerDocument().createElement("div");
            Element text = div.getOwnerDocument().createElement("color1");
            text.setTextContent(new String(message.getBytes(StandardCharsets.UTF_8))); // 띄워쓰끼 문제

            div.appendChild(text);
            body.appendChild(div);
//
//            for (String imageId : imageIds) {
//                String contentId = imageCIDList.get(imageIds.indexOf(imageId));
//
//                Element img = div.getOwnerDocument().createElement("img");
//                img.setAttribute("id", imageId);
//                img.setAttribute("src", contentId);
//
//                div.appendChild(img);
//            }

            return document;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public static String getRandomContentID(String randomNumber) {
        return DateUtil.getDate() + CommonUtil.getStringConsistOfRandomNumber(8);
    }

    public static String getContentID(int index, long randomNumber) {
        return randomNumber + "1332667110" + index;
    }


    /* Legacy SelfCode 참조 */
    public static String getCIDSID() {
        return "1332667110";
//        String time = DateUtil.getDate("yMmddHHmmss");
//        time = time.length() > 11 ? time.substring(time.length() - 11) : time;
//        return "0" + time;
    }
}
