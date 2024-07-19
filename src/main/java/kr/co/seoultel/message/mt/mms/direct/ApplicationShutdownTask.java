package kr.co.seoultel.message.mt.mms.direct;



import kr.co.seoultel.message.mt.mms.direct.modules.HeartBeatClient;
import kr.co.seoultel.message.mt.mms.direct.modules.MessageConsumer;
import kr.co.seoultel.message.mt.mms.direct.modules.ReportProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationShutdownTask implements ApplicationListener<ContextClosedEvent> {

    private final HeartBeatClient heartBeatClient;
    private final MessageConsumer messageConsumer;

    private final ReportProcessor reportProcessor;


    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        Application.stopApplicatiaon();

        Application.stopApplicatiaon();

        heartBeatClient.destroy();
        messageConsumer.destroy();

        reportProcessor.destroy();
    }
}