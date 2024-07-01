package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.mt.mms.core_module.common.property.RabbitMqProperty;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.image.ImageService;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisConnectionChecker;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
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
    public PersistenceManager persistenceManager(RedisService redisService) {
        return new PersistenceManager(redisService);
    }

    @Bean
    public RedisConnectionChecker redisConnectionChecker(RedisConnectionFactory redisConnectionFactory) {
        return new RedisConnectionChecker(redisConnectionFactory);
    }
}
