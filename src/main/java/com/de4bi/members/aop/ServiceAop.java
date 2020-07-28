package com.de4bi.members.aop;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.exception.MapperException;
import com.de4bi.common.exception.ServiceException;

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

    /**
     * Service전/후를 감싸는 AOP입니다. Service메서드 호출 로깅 및 응답, 예외상황을 핸들링합니다.
     * 
     * @param <T> - ApiResult클래스를 상속한 제너릭 클래스입니다.
     * @param pjp - {@code @Around} AOP의 필수 인자입니다. 
     * @return Service로부터 생성된 ApiResult<?>를 반환합니다.
     */
    @Around("execution(* com.de4bi.members.service..*.*(..))")
    public ApiResult<?> aroundService(ProceedingJoinPoint pjp) {
        // 초기화
        final long bgnTime = System.currentTimeMillis();
        final String oldLayer = MDC.get("layer");
        MDC.put("layer", "SVC");

        // 접근 로깅
        final Signature sign = pjp.getSignature();
        final String reqFunc = "> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("--- Service begin! ---");
        logger.info(reqFunc);

        // 서비스 수행
        ApiResult<?> svcResult = null;
        try {
            // [Note] 개발자의 실수로 일반적인 Service에서 ApiResult<?>를 반환하지 않는 경우
            // ClassCastException이 발생합니다. 반드시 위 클래스를 반환하도록 설계하셔야 합니다.
            svcResult = (ApiResult<?>) pjp.proceed();
        }
        catch (MapperException | ApiException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new ServiceException(e);
        }

        // 결과 로깅 및 반환
        logger.info("< SvcResult: '" + (svcResult == null ? null : svcResult.toString()) + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("--- Service end! --- (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return svcResult;
    }
}