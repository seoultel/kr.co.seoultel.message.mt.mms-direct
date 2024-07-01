package kr.co.seoultel.message.mt.mms.direct.modules;

import kr.co.seoultel.message.mt.mms.core.util.CommonUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.property.RabbitMqProperty;
import kr.co.seoultel.message.mt.mms.core_module.modules.heartBeat.HeartBeatProtocol;
import kr.co.seoultel.message.mt.mms.direct.Application;
import kr.co.seoultel.message.mt.mms.direct.config.HeartBeatConfig;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static kr.co.seoultel.message.mt.mms.core.common.constant.Constants.SECOND;

@Slf4j
@Component
public class HeartBeatClient extends kr.co.seoultel.message.mt.mms.core_module.modules.heartBeat.client.DefaultHeartBeatClient {

    public HeartBeatClient(RabbitMqProperty rabbitMqProperty) {
        super(rabbitMqProperty);
        hStatus = HeartBeatProtocol.HEART_SUCCESS;
    }

    @Override
    public void run() {
        if (SenderConfig.IS_DUMMY) {
            return;
        }

        createSession();
        while (Application.isStarted()) {
            while (!isChannelBound()) {
                connectTo();

                if (!isChannelBound()) CommonUtil.doThreadSleep(HeartBeatConfig.RECONNECT_TIME * SECOND);
            }

            CommonUtil.doThreadSleep(10 * SECOND);
        }
    }
}
