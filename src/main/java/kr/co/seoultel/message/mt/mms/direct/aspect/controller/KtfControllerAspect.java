package kr.co.seoultel.message.mt.mms.direct.aspect.controller;

import jakarta.xml.soap.SOAPException;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.PersistenceException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.ktf.KtfDeliveryReportReqMessage;
import kr.co.seoultel.message.mt.mms.core.util.ConvertorUtil;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.RedisUtil;
import kr.co.seoultel.message.mt.mms.direct.ktf.KtfCondition;
import kr.co.seoultel.message.mt.mms.direct.lgt.LgtCondition;
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
@Conditional(KtfCondition.class)
public class KtfControllerAspect {

    private final RedisService redisService;
    private final HashMapStorage<String, MessageDelivery> deliveryStorage;

    @AfterThrowing(pointcut = "execution(* kr.co.seoultel.message.mt.mms.direct.controller.ktf.KtfController.*(..))",
            throwing = "exception")
    public void catchIOAndSoapException(Exception exception) {
        if (exception instanceof IOException | exception instanceof SOAPException) {
            log.error("[SYSTEM] I/O or soap problem during report handling", exception);
        }

        if (exception instanceof PersistenceException) {
            KtfDeliveryReportReqMessage ktfDeliveryReportReqMessage = (KtfDeliveryReportReqMessage) ((PersistenceException) exception).getSource();
            String messageId = ktfDeliveryReportReqMessage.getMessageId();
            Optional<String> optional = redisService.getSafely(RedisUtil.getRedisKeyOfMessage(), messageId);
            if (optional.isPresent()) {
                MessageDelivery messageDelivery = ConvertorUtil.convertJsonToObject(optional.get(), MessageDelivery.class);
                deliveryStorage.putIfAbsent(messageId, messageDelivery);
                log.warn("[SYSTEM] Re-add redis to deliveryStorage of message[{}]", messageDelivery);
            } else {
                log.error("[SYSTEM] Fail to find message[dstMsgId : {}] in redis, received request[{}]", messageId, ktfDeliveryReportReqMessage);
            }
        }
    }

}
