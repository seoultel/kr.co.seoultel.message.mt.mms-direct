package kr.co.seoultel.message.mt.mms.direct.skt;

import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.core_module.dto.endpoint.Endpoint;

public class SktEndpoint extends Endpoint {
    public SktEndpoint(HttpClientProperty property) {
        super(property.getIp(), property.getPort(), Constants.SKT_REQUEST_URL);
    }
}
