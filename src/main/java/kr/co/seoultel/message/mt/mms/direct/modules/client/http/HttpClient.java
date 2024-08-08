package kr.co.seoultel.message.mt.mms.direct.modules.client.http;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.TpsOverExeption;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;

import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.utils.MMSReportUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
@Getter
public class HttpClient {

    protected final int tps;
    protected final HttpClientHandler handler;

    protected final MultiMediaService fileService;

    public HttpClient(int tps, HttpClientHandler handler, MultiMediaService fileService) {
        this.tps = tps;
        this.handler = handler;

        this.fileService = fileService;
    }

    public void doSubmit(InboundMessage inboundMessage) throws FileServerException, TpsOverExeption, NAckException, MCMPSoapRenderException {
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();
        try {
            String groupCode = messageDelivery.getGroupCode();
            List<String> imageIds = FallbackUtil.getMediaFiles(messageDelivery);

            if (!imageIds.isEmpty()) {
                // 메세지가 이미지를 4개 이상 가지고 있는지 확인
                fileService.checkAttachedImageCount(inboundMessage, imageIds);

                /*
                 * 만료된 이미지가 있다면 ImageExpiredException 예외 발생
                 * 레디스와의 커넥션 문제로 만료 여부를 판별 할 수 없다면
                 * ImageUtil.saveImagesByImageId(umsMsgId, groupCode, undownloadedImageIdSet); 에서 파일 서버에 요청할 때 이미지 만료 여부를 알 수 있음.
                 */
                fileService.hasExpiredImages(inboundMessage, groupCode, imageIds);

                // 다운로드 되지 않은 이미지
                Set<String> undownloadedImageIdSet = fileService.getUndownloadImageIdSet(groupCode, imageIds);
                if (!undownloadedImageIdSet.isEmpty()) {
                    fileService.saveImagesByImageId(inboundMessage, undownloadedImageIdSet);
                }
            }

            handler.isTpsOver();
            handler.doSubmit(inboundMessage);
        } catch (MCMPSoapRenderException e) {
            log.error(e.getMessage(), e.getOrigin());

            DeliveryType deliveryType = FallbackUtil.isFallback(messageDelivery) ? DeliveryType.FALLBACK_SUBMIT_ACK : DeliveryType.SUBMIT_ACK;
            MMSReportUtil.handleSenderException(messageDelivery, e.getMessage(), e.getMnoMessage(), deliveryType);

            MrReport mrReport = new MrReport(deliveryType, messageDelivery);
            handler.addReportQueue(mrReport);
            log.info("[[SOAP] Fail to create soap message, add report-queue to message-delivery] Successfully add SubmitAck[{}] in reportQueue", messageDelivery);

            inboundMessage.basicAck();
        }
    }

    @Override
    public String toString() {
        return "HttpClient{" +
                "handler=" + handler +
                '}';
    }
}
