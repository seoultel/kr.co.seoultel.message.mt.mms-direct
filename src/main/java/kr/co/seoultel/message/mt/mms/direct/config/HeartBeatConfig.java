package kr.co.seoultel.message.mt.mms.direct.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/* HEART-BEAT 관련 상수 */
@Slf4j
@Configuration
public class HeartBeatConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultHeartBeatConfig {

    @Value("${heart-beat.host}")
    public void setHost(String host) {
        HOST = host;
    };

    @Value("${heart-beat.port}")
    public void setPort(int port) {
        PORT = port;
    };

    @Value("${heart-beat.reconnect-time}")
    public void setReconnectTime(int reconnectTime) {
        RECONNECT_TIME = reconnectTime;
    };

    @Value("${heart-beat.expire-time}")
    public void setExpiredTime(int expireTime) {
        EXPIRE_TIME = expireTime;
    }

}