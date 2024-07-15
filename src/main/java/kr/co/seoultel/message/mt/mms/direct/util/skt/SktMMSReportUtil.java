package kr.co.seoultel.message.mt.mms.direct.util.skt;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.core.dto.Result;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.protocol.SktProtocol;
import kr.co.seoultel.message.mt.mms.core.messages.Message;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSoapMessage;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSubmitResMessage;
import kr.co.seoultel.message.mt.mms.core.messages.smtnt.delivery.SmtntDeliveryAckMessage;
import kr.co.seoultel.message.mt.mms.core.messages.smtnt.report.SmtntReportMessage;
import kr.co.seoultel.message.mt.mms.core.util.DateUtil;
import kr.co.seoultel.message.mt.mms.core.util.FallbackUtil;
import kr.co.seoultel.message.mt.mms.core_module.utils.MMSReportUtil;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;

import java.util.*;

import static kr.co.seoultel.message.mt.mms.core.common.protocol.LghvProtocol.DELIVERY_ACK_RESULT_E_OK;
import static kr.co.seoultel.message.mt.mms.core.common.protocol.LghvProtocol.REPORT_RESULT_E_SENT;

public class SktMMSReportUtil extends MMSReportUtil<SktSoapMessage> {
    @Override
    public void prepareToSubmitAck(MessageDelivery messageDelivery, SktSoapMessage sktSoapMessage) {
        SktSubmitResMessage sktSubmitResMessage = (SktSubmitResMessage) sktSoapMessage;

        setDeliveryTypeAndStateAtSubmitAck(sktSubmitResMessage.isSuccess(), messageDelivery);
        FallbackUtil.setResult(messageDelivery, getSubmitAckResult(messageDelivery, sktSubmitResMessage));
    }

    @Override
    public Map<String, Object> getSubmitAckResult(MessageDelivery messageDelivery, SktSoapMessage sktSoapMessage) {
        SktSubmitResMessage sktSubmitResMessage = (SktSubmitResMessage) sktSoapMessage;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());

        result.put(Result.MNO_CD, SenderConfig.GROUP);
        result.put(Result.MNO_RESULT, sktSubmitResMessage.getStatusCode());
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, SktUtil.getStatusCodeKor(sktSubmitResMessage.getStatusCode()));
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());

        return result;
    }

    @Override
    public void prepareToReport(MessageDelivery messageDelivery, SktSoapMessage sktSoapMessage) {
        SktDeliveryReportReqMessage sktDeliveryReportReqMessage = (SktDeliveryReportReqMessage) sktSoapMessage;

        setDeliveryTypeAndStateAtReport(sktDeliveryReportReqMessage.isSuccess(), messageDelivery); // set DeliveryType & DeliveryState
        FallbackUtil.setResult(messageDelivery, getReportResult(sktDeliveryReportReqMessage.isSuccess(), messageDelivery, sktDeliveryReportReqMessage));
    }

    @Override
    public Map<String, Object> getReportResult(boolean isSuccess, MessageDelivery messageDelivery, SktSoapMessage sktSoapMessage) {
        SktDeliveryReportReqMessage sktDeliveryReportReqMessage = (SktDeliveryReportReqMessage) sktSoapMessage;

        // if message is fallback -> fallback result, else result;
        Map<String, Object> result = Objects.requireNonNullElse(FallbackUtil.getResult(messageDelivery), new LinkedHashMap<>());
        String resultMessage = isSuccess ? "Successed to Send Message" : "Failed to Send Message";

        // RESULT
        result.put(Result.MNO_CD, SenderConfig.GROUP); // Reporter 의 리포트 처리를 위해 MNO_CD에 "LGHV" 가 들어가야만 한다.
        result.put(Result.MNO_RESULT, sktDeliveryReportReqMessage.getStatusCode()); // 원본코드
        result.put(Result.SETTLE_CODE, SenderConfig.NAME);
        result.put(Result.MESSAGE, resultMessage);
        result.put(Result.PFM_SND_DTTM, DateUtil.getDate());
        result.put(Result.PFM_RCV_DTTM, DateUtil.getDate());

        List<String> triedMnos = (List<String>) result.getOrDefault(Result.TRIED_MNOS, new ArrayList<>());
        triedMnos.add(Constants.SKT);
        result.put(Result.TRIED_MNOS, triedMnos);

        return result;
    }
}
