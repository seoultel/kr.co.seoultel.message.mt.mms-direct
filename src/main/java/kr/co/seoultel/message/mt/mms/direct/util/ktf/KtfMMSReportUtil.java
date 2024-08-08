package kr.co.seoultel.message.mt.mms.direct.util.ktf;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.core.dto.Result;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfSoapMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfSubmitResMessage;
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
        KtfSubmitResMessage ktfSubmitResMessage = (KtfSubmitResMessage) ktfSoapMessage;
        boolean isSuccessToSend = ktfSubmitResMessage.getStatusCode().equals(KtfProtocol.KTF_SUBMIT_ACK_SUCCESS_RESULT);

        setDeliveryTypeAndStateAtSubmitAck(isSuccessToSend, messageDelivery);
        FallbackUtil.setResult(messageDelivery, getSubmitAckResult(messageDelivery, ktfSubmitResMessage));
    }

    @Override
    protected Map<String, Object> getSubmitAckResult(MessageDelivery messageDelivery, KtfSoapMessage ktfSoapMessage) {
        KtfSubmitResMessage ktfSubmitResMessage = (KtfSubmitResMessage) ktfSoapMessage;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());

        result.put(Result.MNO_CD, KTF);
        result.put(Result.MNO_RESULT, ktfSubmitResMessage.getStatusCode());
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, KtfUtil.getSubmitAckStatusCodeKor(ktfSubmitResMessage.getStatusCode()));
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());

        List<String> triedMnos = (List<String>) result.getOrDefault(Result.TRIED_MNOS, new ArrayList<>());
        if (!triedMnos.contains(KTF)) triedMnos.add(KTF);

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
        result.put(Result.MNO_CD, KTF); // Reporter 의 리포트 처리를 위해 MNO_CD에 "KTF" 가 들어가야만 한다.
        result.put(Result.MNO_RESULT, ktfDeliveryReportReqMessage.getMmStatus()); // 원본코드
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, resultMessage);
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());
        result.put(Result.PFM_RCV_DTTM, DateUtil.getDate());

        List<String> triedMnos = (List<String>) result.getOrDefault(Result.TRIED_MNOS, new ArrayList<>());
        if (!triedMnos.contains(KTF)) triedMnos.add(KTF);

        return result;
    }
}
