package kr.co.seoultel.message.mt.mms.direct.skt;

import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class SktCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String telecom = context.getEnvironment().getProperty("sender.telecom");
        return Objects.requireNonNull(telecom).equalsIgnoreCase(Constants.SKT);
    }
}