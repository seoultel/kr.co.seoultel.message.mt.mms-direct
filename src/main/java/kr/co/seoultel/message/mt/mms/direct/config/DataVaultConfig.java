package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Configuration
public class DataVaultConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultDataVaultConfig {

    @Value("${sender.data-vault.paths.persistence}")
    public void setPersistenceFilePath(String persistenceFilePath) {
        PERSISTENCE_FILE_PATH = persistenceFilePath;
    }

    @Value("${sender.data-vault.paths.republish}")
    public void setRepublishFilePath(String republishFilePath) {
        REPUBLISH_FILE_PATH = republishFilePath;
    }

    @Value("${sender.data-vault.paths.message-history}")
    public void setMessageHistoriesFilePath(String messagehistoriesFilePath) {
        MESSAGE_HISTORIES_FILE_PATH = messagehistoriesFilePath;
    }

    @Value("${sender.data-vault.paths.report}")
    public void getReportFilePath(String reportFilePath) {
        REPORT_FILE_PATH = reportFilePath;
    }


    @Bean
    public ConcurrentLinkedQueue<MrReport> reportQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    @Bean
    public ConcurrentLinkedQueue<MessageDelivery> republishQueue() {
        return new ConcurrentLinkedQueue<>();
    }

}
