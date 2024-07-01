package kr.co.seoultel.message.mt.mms.direct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties()
public class Application {

    private static boolean isStop = false;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.addListeners(new ApplicationPidFileWriter("application.pid"));
        application.run(args);
    }

    public static void startApplicatiaon() {
        isStop = false;
    }

    public static void stopApplicatiaon() {
        isStop = true;
    }

    public static boolean isStarted() {
        return !isStop;
    }
}
