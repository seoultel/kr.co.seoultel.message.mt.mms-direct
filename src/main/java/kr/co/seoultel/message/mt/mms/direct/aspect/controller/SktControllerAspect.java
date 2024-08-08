package kr.co.seoultel.message.mt.mms.direct.aspect.controller;

import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.ConvertorUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.RedisUtil;
import kr.co.seoultel.message.mt.mms.direct.skt.SktCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Conditional(SktCondition.class)
public class SktControllerAspect {

    private final RedisService redisService;
    private final HashMapStorage<String, MessageDelivery> deliveryStorage;

    @AfterThrowing(pointcut = "execution(* kr.co.seoultel.message.mt.mms.direct.controller.skt.SktController.*(..))",
            throwing = "exception")
    public void catchIOAndSoapException(Exception exception) {
        if (exception instanceof IOException) {
            log.error("[SYSTEM] I/O or soap problem during report handling", exception);
        } else if (exception instanceof SOAPException) {
            log.error("[SYSTEM] Fail to create SktDeliveryReportReq message during report handling", exception);
        }
    }
}
