package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core_module.common.property.RabbitMqProperty;

import kr.co.seoultel.message.mt.mms.core_module.modules.MMSScheduler;
import kr.co.seoultel.message.mt.mms.core_module.modules.multimedia.MultiMediaService;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.modules.HeartBeatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* SENDER 관련 상수 */
@Slf4j
@Configuration
public class SenderConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultSenderConfig {

    @Bean
    public RabbitMqProperty rabbitMqProperty() {
        return new RabbitMqProperty();
    }

    @Bean
    public MultiMediaService fileService(RedisService redisService, HashMapStorage<String, String> fileStorage) {
        return new MultiMediaService(redisService, fileStorage);
    }

}
