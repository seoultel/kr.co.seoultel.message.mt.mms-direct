package kr.co.seoultel.message.mt.mms.direct.test;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.core.dto.Result;
import kr.co.seoultel.message.core.dto.mms.Submit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TestPublisher {
    private final RabbitTemplate testTemplate;

    public TestPublisher(@Qualifier("testTemplate") RabbitTemplate testTemplate) {
        this.testTemplate = testTemplate;
    }

    @SneakyThrows
    public void publish(int count, TestMessage testMessage){
        log.info("message : {}", testMessage.getMsg());
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        var future =  CompletableFuture.runAsync(() -> {
            for(int i = 1; i <= count; i++ ){
                var message = MessageDelivery.builder()
                        .srcMsgId(TestUtil.generateUniqueNumberString())
                        .umsMsgId(TestUtil.generateUniqueNumberString())
                        .dstMsgId(TestUtil.generateUniqueNumberString())
//                        .umsMsgId(String.valueOf(i))
                        .cmpMsgId(TestUtil.generateUniqueNumberString())
                        .groupCode(testMessage.getGroupCode())
                        .channel("MMS")
                        .serviceProvider(testMessage.getTelecom())
                        .sender(testMessage.getSender())
//                        .sender(testMessage.getSender().getBytes(Charset.forName("euc-kr")).toString())
                        .callback(testMessage.getCallback())
                        .receiver(testMessage.getReceiver())
                        .content(Submit.builder()
                                .message(testMessage.getMsg())
                                .subject(testMessage.getSubject())
                                .mediaFiles(testMessage.getMediaFiles())
                                .originCode(testMessage.getOriginCode())
                                .build().toMap())
                        .result(Result.builder()
                                .srcSndDttm(TestUtil.getDate(0))
                                .build().toMap())
                        .reportTo("http://192.168.50.24:8001")
                        .build();
                try {
                    testTemplate.convertAndSend(message);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }, executorService);
        future.get();
        executorService.shutdown();
        log.info("{} messages published", count);
    }
}
