package kr.co.seoultel.message.mt.mms.direct.util.ktf;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.core.dto.Result;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfResMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfSoapMessage;
import kr.co.seoultel.message.mt.mms.core.util.DateUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.utils.MMSReportUtil;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;
import kr.co.seoultel.message.mt.mms.core.common.protocol.KtfProtocol;

import java.util.*;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.*;

public class KtfMMSReportUtil extends MMSReportUtil<KtfSoapMessage> {
    @Override
    public void prepareToSubmitAck(MessageDelivery messageDelivery, KtfSoapMessage ktfSoapMessage) {
        KtfResMessage ktfResMessage = (KtfResMessage) ktfSoapMessage;
        boolean isSuccessToSend = ktfResMessage.getStatusCode().equals(KtfProtocol.KTF_SUBMIT_ACK_SUCCESS_RESULT);

        setDeliveryTypeAndStateAtSubmitAck(isSuccessToSend, messageDelivery);
        FallbackUtil.setResult(messageDelivery, getSubmitAckResult(messageDelivery, ktfResMessage));
    }

    @Override
    protected Map<String, Object> getSubmitAckResult(MessageDelivery messageDelivery, KtfSoapMessage ktfSoapMessage) {
        KtfResMessage ktfResMessage = (KtfResMessage) ktfSoapMessage;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());

        result.put(Result.MNO_CD, SenderConfig.GROUP);
        result.put(Result.MNO_RESULT, ktfResMessage.getStatusCode());
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, KtfUtil.getSubmitAckStatusCodeKor(ktfResMessage.getStatusCode()));
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());

        return result;
    }

    @Override
    public void prepareToReport(MessageDelivery messageDelivery, KtfSoapMessage ktfSoapMessage) {
        KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = (KtfDeliveryReportReqMessage) ktfSoapMessage;

        setDeliveryTypeAndStateAtReport(ktfDeliveryReportReqMessage.isSuccess(), messageDelivery); // set DeliveryType & DeliveryState
        FallbackUtil.setResult(messageDelivery, getReportResult(ktfDeliveryReportReqMessage.isSuccess(), messageDelivery, ktfDeliveryReportReqMessage));
    }

    @Override
    protected Map<String, Object> getReportResult(boolean isSuccess, MessageDelivery messageDelivery, KtfSoapMessage ktfSoapMessage) {
        KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = (KtfDeliveryReportReqMessage) ktfSoapMessage;

        // if message is fallback -> fallback result, else result;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());
        String resultMessage = isSuccess ? "Success to Send Message" : "Failed to Send Message";

        // RESULT
        result.put(Result.MNO_CD, SenderConfig.GROUP); // Reporter 의 리포트 처리를 위해 MNO_CD에 "LGHV" 가 들어가야만 한다.
        result.put(Result.MNO_RESULT, ktfDeliveryReportReqMessage.getMmStatus()); // 원본코드
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, resultMessage);
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());
        result.put(Result.PFM_RCV_DTTM, DateUtil.getDate());

        List<String> triedMnos = (List<String>) result.getOrDefault(Result.TRIED_MNOS, new ArrayList<>());
        triedMnos.add(KTF);
        result.put(Result.TRIED_MNOS, triedMnos);

        return result;
    }
}
