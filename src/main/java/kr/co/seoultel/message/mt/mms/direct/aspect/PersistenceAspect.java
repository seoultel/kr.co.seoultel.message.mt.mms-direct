package kr.co.seoultel.message.mt.mms.direct.aspect;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.entity.MessageHistory;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisConnectionChecker;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PersistenceAspect {

    private final RedisService redisService;
    private final HashMapStorage<String, MessageHistory> historyStorage;

    @AfterReturning(pointcut = "execution(* kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage.put(..)) && bean(deliveryStorage)")
    public void putAfter(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        String dstMsgId = (String) arguments[0];
        MessageDelivery messageDelivery = (MessageDelivery) arguments[1];

        MessageHistory messageHistory = new MessageHistory(dstMsgId);
        historyStorage.put(dstMsgId, messageHistory);
        log.info("[HISTORY] Successfully saved history of message[{}] ", dstMsgId);

        redisService.putSafely(RedisUtil.getRedisKeyOfMessage(), dstMsgId, messageDelivery);
        log.info("[REDIS] Successfully saved message[{}] in redis", dstMsgId);
    }

    @AfterReturning(pointcut = "execution(* kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage.remove(..)) && bean(deliveryStorage)")
    public void removeAfter(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        String dstMsgId = (String) arguments[0];

        historyStorage.remove(dstMsgId);
        log.info("[HISTORY] Successfully removed history of message[{}] ", dstMsgId);

        redisService.deleteSafely(RedisUtil.getRedisKeyOfMessage(), dstMsgId);
        log.info("[REDIS] Successfully removed message[{}] in redis", dstMsgId);
    }
}
