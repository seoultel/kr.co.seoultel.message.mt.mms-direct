package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.mt.mms.core_module.modules.consumer.AbstractConsumer;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReportService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* REPORT 관련 상수 */
@Configuration
public class ReportConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultReportConfig {

    public ReportConfig(AbstractConsumer consumer, RabbitMQConfig rabbitMQConfig) {
        super(consumer, rabbitMQConfig);
    }

    @Bean
    public MrReportService mrReportService(AbstractConsumer consumer, RabbitMQConfig rabbitMQConfig) {
        return new MrReportService(consumer, rabbitMQConfig);
    }

}
