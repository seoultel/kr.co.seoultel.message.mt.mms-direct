package kr.co.seoultel.message.mt.mms.direct.controller.skt;

import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core_module.modules.PersistenceManager;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.skt.util.SktMMSReportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.skt.condition.SktCondition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RestController
@RequiredArgsConstructor
@Conditional(SktCondition.class)
public class SktController {
    private final SktMMSReportUtil sktMMSReportUtil = new SktMMSReportUtil();

    private final PersistenceManager persistenceManager;
    private final ConcurrentLinkedQueue<MrReport> reportQueue;
    private final ConcurrentLinkedQueue<MessageDelivery> republishQueue;

    /*
     * DeliveryReportReq
     */
    @PostMapping("/")
    public ResponseEntity<String> receiveMM7DeliveryReportReq(HttpServletRequest httpServletRequest) throws IOException, SOAPException {
        return null;
    }
}
