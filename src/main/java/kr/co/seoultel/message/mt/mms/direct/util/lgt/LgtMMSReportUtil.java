package kr.co.seoultel.message.mt.mms.direct.util.lgt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.core.dto.Result;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtSoapMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtSubmitResMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSoapMessage;
import kr.co.seoultel.message.mt.mms.core.util.DateUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.utils.MMSReportUtil;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;
import kr.co.seoultel.message.mt.mms.direct.util.skt.SktUtil;

import java.util.*;

public class LgtMMSReportUtil extends MMSReportUtil<LgtSoapMessage> {
    @Override
    protected void prepareToSubmitAck(MessageDelivery messageDelivery, LgtSoapMessage lgtSoapMessage) {
        LgtSubmitResMessage lgtSubmitResMessage = (LgtSubmitResMessage) lgtSoapMessage;

        setDeliveryTypeAndStateAtSubmitAck(lgtSubmitResMessage.isSuccess(), messageDelivery);
        FallbackUtil.setResult(messageDelivery, getSubmitAckResult(messageDelivery, lgtSubmitResMessage));
    }

    @Override
    protected Map<String, Object> getSubmitAckResult(MessageDelivery messageDelivery, LgtSoapMessage lgtSoapMessage) {
        LgtSubmitResMessage lgtSubmitResMessage = (LgtSubmitResMessage) lgtSoapMessage;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());

        result.put(Result.MNO_CD, SenderConfig.GROUP);
        result.put(Result.MNO_RESULT, lgtSubmitResMessage.getStatusCode());
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, SktUtil.getStatusCodeKor(lgtSubmitResMessage.getStatusCode()));
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());

        return result;
    }

    @Override
    public void prepareToReport(MessageDelivery messageDelivery, LgtSoapMessage lgtSoapMessage) {
        LgtDeliveryReportReqMessage lgtDeliveryReportReqMessage = (LgtDeliveryReportReqMessage) lgtSoapMessage;

        setDeliveryTypeAndStateAtReport(lgtDeliveryReportReqMessage.isSuccess(), messageDelivery); // set DeliveryType & DeliveryState
        FallbackUtil.setResult(messageDelivery, getReportResult(lgtDeliveryReportReqMessage.isSuccess(), messageDelivery, lgtDeliveryReportReqMessage));
    }

    @Override
    protected Map<String, Object> getReportResult(boolean isSuccess, MessageDelivery messageDelivery, LgtSoapMessage lgtSoapMessage) {
        LgtDeliveryReportReqMessage lgtDeliveryReportReqMessage = (LgtDeliveryReportReqMessage) lgtSoapMessage;

        // if message is fallback -> fallback result, else result;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());
        String resultMessage = isSuccess ? "Successed to Send Message" : "Failed to Send Message";


        // RESULT
        result.put(Result.MNO_CD, SenderConfig.GROUP); // Reporter 의 리포트 처리를 위해 MNO_CD에 "LGHV" 가 들어가야만 한다.
        result.put(Result.MNO_RESULT, lgtDeliveryReportReqMessage.getMmStatus()); // 원본코드
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, resultMessage);
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());
        result.put(Result.PFM_RCV_DTTM, DateUtil.getDate());

        List<String> triedMnos = (List<String>) result.getOrDefault(Result.TRIED_MNOS, new ArrayList<>());
        triedMnos.add(Constants.LGT);
        result.put(Result.TRIED_MNOS, triedMnos);

        return result;
    }
}
