package kr.co.seoultel.message.mt.mms.direct.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
    private final TestPublisher testPublisher;

    public TestController(TestPublisher testPublisher) {
        this.testPublisher = testPublisher;
    }

    @PostMapping("/test/publish")
    public ResponseEntity<?> publishForTest(@RequestParam int count,
                                            @RequestBody(required = false) TestMessage testMessage){
        log.info("Test Publish start, count : {}", count);
        testPublisher.publish(count, testMessage);

        return ResponseEntity.ok(null);
    }
}
