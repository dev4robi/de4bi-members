package com.de4bi.members.aop;

import com.de4bi.common.exception.MapperException;

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
public class MapperAop {

    private static final Logger logger = LoggerFactory.getLogger(MapperAop.class);

    /**
     * Mapper전/후를 감싸는 AOP입니다. Mapper메서드 호출 및 응답, 예외상황을 핸들링합니다.
     * 
     * @param pjp - @Around AOP의 필수 인자입니다.
     * @return DB로부터 획득한 값을 
     */
    @Around("execution(* com.de4bi.members.db.mapper..*.*(..))")
    public Object aroundMapper(ProceedingJoinPoint pjp) {
        // 초기화
        final long bgnTime = System.currentTimeMillis();
        final String oldLayer = MDC.get("layer");
        MDC.put("layer", "MPR");

        // 접근 로깅
        final Signature sign = pjp.getSignature();
        final String reqFunc = "> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("### Mapper begin! ###");
        logger.info(reqFunc);

        // 매퍼 수행
        Object mprResult = null;
        try {
            mprResult = pjp.proceed();
        }
        catch (Throwable e) {
            throw new MapperException(e);
        }

        // 결과 로깅 및 반환
        logger.info("< MprResult: '" + (mprResult == null ? null : mprResult.toString()) + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("### Mapper end! ### (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return mprResult;
    }
}