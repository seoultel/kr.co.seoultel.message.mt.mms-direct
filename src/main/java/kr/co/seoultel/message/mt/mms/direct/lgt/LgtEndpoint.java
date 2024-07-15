package kr.co.seoultel.message.mt.mms.direct.lgt;

import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.core_module.dto.endpoint.Endpoint;

public class LgtEndpoint extends Endpoint {
    public LgtEndpoint(HttpClientProperty property) {
        super(property.getIp(), property.getPort(), Constants.LGT_REQUEST_URL);
    }
}
