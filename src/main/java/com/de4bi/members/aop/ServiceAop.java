package com.de4bi.members.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceAop {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAop.class);

    @Around("execution(* com.de4bi.members.service..*.*(..))")
    public String aroundService(ProceedingJoinPoint pjp) {
        // 초기화
        final long bgnTime = System.currentTimeMillis();
        final String oldLayer = MDC.get("layer");
        MDC.put("layer", "SVC");

        // 접근 로깅
        final Signature sign = pjp.getSignature();
        final String reqFunc = "> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("--- API Controller begin! ---");
        logger.info(reqFunc);

        // 서비스 수행
        Object svcResult = null;

        try {
            svcResult = pjp.proceed();

            if (svcResult == null) {
                svcResult = "{}";
            }
        }
        catch (Throwable e) {
            logger.error("Exception!", e);
        }

        // 결과 로깅 및 반환
        final String svcResultStr = svcResult.toString();
        logger.info("< Result: '" + svcResultStr + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("--- Service end! --- (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return svcResultStr;
    }
}