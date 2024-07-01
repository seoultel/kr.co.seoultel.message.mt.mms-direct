package kr.co.seoultel.message.mt.mms.direct.modules.client.http;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.image.ImageService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
@Getter
public class HttpClient {

    protected final HttpClientHandler handler;
    protected final PersistenceManager persistenceManager;

    public HttpClient(HttpClientHandler handler) {
        this.handler = handler;
        this.persistenceManager = handler.persistenceManager;
    }

    public void doSubmit(InboundMessage inboundMessage) throws Exception {
        MessageDelivery messageDelivery = inboundMessage.getMessageDelivery();

        String groupCode = messageDelivery.getGroupCode();
        List<String> imageIds = FallbackUtil.getMediaFiles(messageDelivery);

        // if (!imageIds.isEmpty()) {
        //     // 메세지가 이미지를 4개 이상 가지고 있는지 확인
        //     ImageService.checkAttachedImageCount(inboundMessage, imageIds);
        //     // 다운로드 되지 않은 이미지
        //     Set<String> undownloadedImageIdSet = ImageService.getUndowndloadedImageIdSet(groupCode, imageIds);
        //     /*
        //      * 만료된 이미지가 있다면 ImageExpiredException 예외 발생
        //      * 레디스와의 커넥션 문제로 만료 여부를 판별 할 수 없다면
        //      * ImageUtil.saveImagesByImageId(umsMsgId, groupCode, undownloadedImageIdSet); 에서 파일 서버에 요청할 때 이미지 만료 여부를 알 수 있음.
        //      */
        //     persistenceManager.hasExpiredImages(inboundMessage, groupCode, undownloadedImageIdSet);
        //     if (!undownloadedImageIdSet.isEmpty()) {
        //         ImageService.saveImagesByImageId(inboundMessage, undownloadedImageIdSet);
        //     }
        // }

        handler.isTpsOver();
        handler.doSubmit(inboundMessage);
    }

    @Override
    public String toString() {
        return "HttpClient{" +
                "handler=" + handler +
                '}';
    }
}
