package com.de4bi.members.aop;

import com.de4bi.common.data.ApiResult;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceAop {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAop.class);

    @Around("execution(* com.de4bi.members.service..*.*(..))")
    public <T extends ApiResult> ResponseEntity<T> aroundService(ProceedingJoinPoint pjp) {
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
        ResponseEntity<T> svcResult = null;
        try {
            try {
                // [Note] 의도된 SuppressWarnings입니다. 개발자의 실수로 일반적인 Service에서
                // ResponseEntity<T extends ApiResult>를 반환하지 않는다면, 설계상 오류입니다.
                // 반드시 위의 값을 반환하도록 설계하도록 해주시길 바랍니다.
                @SuppressWarnings("unchecked")
                final ResponseEntity<T> tempResult = (ResponseEntity<T>) pjp.proceed();
                svcResult = tempResult;
            }
            catch (ClassCastException e) {
                logger.error("All Service components must return 'ResponseEntity<T extends ApiResult>'!");
            }
        }
        catch (Throwable e) {
            logger.error("Exception!", e);
        }

        // 결과 로깅 및 반환
        logger.info("< SvcResult: '" + svcResult == null ? null : svcResult.toString() + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("--- Service end! --- (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return svcResult;
    }
}