package kr.co.seoultel.message.mt.mms.direct.controller.lgt;

import org.springframework.context.annotation.Conditional;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.lgt.condition.LgtCondition;

@Conditional(LgtCondition.class)
public class LgtController {
}
