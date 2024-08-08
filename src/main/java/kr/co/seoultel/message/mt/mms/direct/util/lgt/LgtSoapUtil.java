package kr.co.seoultel.message.mt.mms.direct.util.lgt;

import jakarta.activation.DataHandler;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.soap.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtSubmitReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core.util.ValidateUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.AttachedImageSizeOverException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.ImageUtil;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.util.SoapUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.EUC_KR;

@Slf4j
public class LgtSoapUtil extends SoapUtil {

    public LgtSoapUtil(HttpClientProperty property, HashMapStorage<String, String> fileStorage) {
        super(property, fileStorage);
    }

    @Override
    public String createSOAPMessage(InboundMessage inboundMessage) throws MCMPSoapRenderException {
        try {
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

            LgtSubmitReqMessage lgtSubmitReqMessage = LgtSubmitReqMessage.builder()
                    .tid("tid")
                    .vasId(property.getCpidInfo().getCpid())
                    .vaspId(property.getVaspId())
                    .subject(subject)
                    .callback(callback)
                    .receiver(receiver)
                    .originCode(originCode)
                    .build();


            /*  Create MimeParts  */
            byte[] bytes;
            MimeMultipart mimeMultipart = new MimeMultipart("mixed");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            SOAPMessage soapMessage = lgtSubmitReqMessage.toSOAPMessage();

            if (hasMessage) {
                MimeBodyPart textMimeBodyPart = createTextMimeBodypart(message);
                mimeMultipart.addBodyPart(textMimeBodyPart);
            }

            for (String imageId : imageIds) {
                String imagePath = fileStorage.get(ImageUtil.getImageKey(groupCode, imageId));
                byte[] imageBytes = ImageUtil.getImageBytes(imagePath);

                int mediaFileSize = imageBytes.length;
                if (mediaFileSize > Constants.STANDARD_IMAGE_MAX_SIZE) {
                    throw new AttachedImageSizeOverException(inboundMessage);
                }

                MimeBodyPart imageMimeBodyPart = createImageMimeBodypart(imageId, imageBytes);
                mimeMultipart.addBodyPart(imageMimeBodyPart);
            }

            mimeMultipart.writeTo(baos);

            bytes = baos.toString(StandardCharsets.UTF_8).getBytes("euc-kr");

            ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, mimeMultipart.getContentType());
            DataHandler dataHandler = new DataHandler(byteArrayDataSource);

            // Attachment
            AttachmentPart attachmentPart = soapMessage.createAttachmentPart(dataHandler);
            attachmentPart.setContentId("<" + Constants.LGT_CONTENT_ID + ">");

            soapMessage.addAttachmentPart(attachmentPart);
            soapMessage.saveChanges();

            // soapMessage to String
            baos.reset();
            soapMessage.writeTo(baos);

            return baos.toString("euc-kr");
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public static MimeBodyPart createTextMimeBodypart(String message) throws MCMPSoapRenderException {
        try {
            MimeBodyPart textMimeBodyPart = new MimeBodyPart();

            textMimeBodyPart.setHeader("Content-Type", "text/plain; charset=\"euc-kr\"");
            textMimeBodyPart.setHeader("Content-Transfer-Encoding", "8bit");

            /*
             *  << 우선 적용 사항>>
             * TODO : X-Kmms-SVCCODE 추가 여부
             *        X-Kmms-SVCCODE 는 Text 에 대해 정보이용료 과금을 해야하는 경우, Text 헤더에 추가하는 값으로 12자리 중 앞 네자리에 CPCODE를 넣어준다.
             *        현재 적혀있는 값인 "800100000000"은 연동규격서에 적혀있는 예시 값으로 확인된다.
             */
            textMimeBodyPart.setHeader("X-Kmms-SVCCODE", "800100000000");

            // TODO : 인코딩 확인
            textMimeBodyPart.setContent(message, "text/plain;charset=euc-kr");
            return textMimeBodyPart;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

    public static MimeBodyPart createImageMimeBodypart(String imageId, byte[] bytes) throws MCMPSoapRenderException {
        try {
            MimeBodyPart imageMimeBodyPart = new MimeBodyPart();

            ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(bytes, "image/jpeg");
            DataHandler dataHandler = new DataHandler(byteArrayDataSource);

            imageMimeBodyPart.setDataHandler(dataHandler);

            imageMimeBodyPart.setHeader("Content-Type", "image/jpeg");
            imageMimeBodyPart.setHeader("Content-Disposition", "attachment; filename=\"" + imageId + "\"");
            imageMimeBodyPart.setHeader("Content-Transfer-Encoding", "base64");
            imageMimeBodyPart.setHeader("X-Kmms-redistribution", "TRUE");
            imageMimeBodyPart.setHeader("Content-Category", "Photo");

            /*
             * << 우선 적용 사항>>
             * TODO : VASP code, 과금 정보, image/audio 배경으로 지정가능 여부, VASP content인지 자작 content인지 여부, Web 노출 허용 여부,
             *        재전송 허용 여부(연동규격서, Appendix 참조)
             */
            imageMimeBodyPart.setHeader("X-Kmms-SVCCODE", "800100000000");

            return imageMimeBodyPart;
        } catch (Exception e) {
            throw new MCMPSoapRenderException("[SOAP] Fail to create soap message, add report-queue to message-delivery", e);
        }
    }

}
