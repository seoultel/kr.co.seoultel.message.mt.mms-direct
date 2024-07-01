package kr.co.seoultel.message.mt.mms.direct.modules.client.http.skt.util;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.messages.Message;
import kr.co.seoultel.message.mt.mms.core_module.utils.MMSReportUtil;

import java.util.Map;

public class SktMMSReportUtil extends MMSReportUtil {


    @Override
    protected void prepareToSubmitAck(MessageDelivery messageDelivery, Message message) {

    }

    @Override
    protected Map<String, Object> getSubmitAckResult(MessageDelivery messageDelivery, Message message) {
        return null;
    }

    @Override
    protected void prepareToReport(MessageDelivery messageDelivery, Message message) {

    }

    @Override
    protected Map<String, Object> getReportResult(boolean b, MessageDelivery messageDelivery, Message message) {
        return null;
    }
}
