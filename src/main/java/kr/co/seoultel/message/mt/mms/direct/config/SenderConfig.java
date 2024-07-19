package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core_module.common.property.RabbitMqProperty;

import kr.co.seoultel.message.mt.mms.core_module.modules.ExpirerService;
import kr.co.seoultel.message.mt.mms.core_module.modules.MMSScheduler;
import kr.co.seoultel.message.mt.mms.core_module.modules.consumer.AbstractConsumer;
import kr.co.seoultel.message.mt.mms.core_module.modules.image.ImageService;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisConnectionChecker;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReportService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/* SENDER 관련 상수 */
@Slf4j
@Configuration
public class SenderConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultSenderConfig {

    @Bean
    public RabbitMqProperty rabbitMqProperty() {
        return new RabbitMqProperty();
    }

    @Bean
    public ImageService imageService(RedisService redisService) {
        return new ImageService(redisService);
    }



    @Bean
    public ExpirerService expirerService(RedisService redisService) {
        return new ExpirerService(redisService);
    }

    @Bean
    public MMSScheduler mmsScheduler(ExpirerService expirerService, QueueStorage<MrReport> reportQueueStorage,
                                     HashMapStorage<String, MessageDelivery> deliveryStorage, HashMapStorage<String, MessageHistory> historyStorage) {
        return new MMSScheduler(expirerService, reportQueueStorage, historyStorage, deliveryStorage);
    }
}
