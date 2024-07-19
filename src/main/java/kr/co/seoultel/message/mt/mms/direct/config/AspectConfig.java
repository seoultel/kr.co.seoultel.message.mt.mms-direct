package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.co.seoultel.message.mt.mms.core_module.common.aspect.RabbitMqAspect;
import kr.co.seoultel.message.mt.mms.core_module.common.aspect.RedisAspect;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;


@Configuration
@RequiredArgsConstructor
public class AspectConfig {

    @Bean
    public RedisAspect redisAspect() {
        return new RedisAspect();
    }

    @Bean
    public RabbitMqAspect rabbitMqAspect(QueueStorage<MrReport> reportQueueStorage) {
        return new RabbitMqAspect(reportQueueStorage);
    }


}
