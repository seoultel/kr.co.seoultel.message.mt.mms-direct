package kr.co.seoultel.message.mt.mms.direct;



import kr.co.seoultel.message.mt.mms.direct.modules.HeartBeatClient;
import kr.co.seoultel.message.mt.mms.direct.modules.MessageConsumer;
import kr.co.seoultel.message.mt.mms.direct.modules.ReportProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartUpTask implements ApplicationListener<ApplicationReadyEvent> {

    private final HeartBeatClient heartBeatClient;
    private final MessageConsumer messageConsumer;

    private final ReportProcessor reportProcessor;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Application.startApplicatiaon();

        heartBeatClient.start();
        reportProcessor.start();    // INIT REPORT-PROCESSOR

        messageConsumer.init();
    }
}