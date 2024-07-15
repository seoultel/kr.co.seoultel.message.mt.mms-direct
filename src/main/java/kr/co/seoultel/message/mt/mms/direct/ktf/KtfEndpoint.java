package kr.co.seoultel.message.mt.mms.direct.ktf;

import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.core_module.dto.endpoint.Endpoint;

public class KtfEndpoint extends Endpoint {

    public KtfEndpoint(HttpClientProperty property) {
        super(property.getIp(), property.getPort(), Constants.KTF_REQUEST_URL);
    }
}
