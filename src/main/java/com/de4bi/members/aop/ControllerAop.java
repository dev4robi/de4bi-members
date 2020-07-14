package com.de4bi.members.aop;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;

import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ControllerAop {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAop.class);

    // @Around("execution(* com.de4bi.members.controller.api..*.*(..))")
    public String aroundApiController(ProceedingJoinPoint pjp) {
        // 초기화
        final long bgnTime = System.currentTimeMillis();
        final String tid = RandomStringUtils.randomAlphanumeric(16);
        final String oldLayer = MDC.get("layer");
        MDC.put("layer", "CTR");
        MDC.put("tid", tid);
        ThreadStorage.put(ApiResult.KEY_TID, tid); // 스레드 스토리지에 'tid'를 꼭 넣어줘야 합니다

        // 접근 로깅
        final ServletRequestAttributes svlReqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpSvlReq = svlReqAttrs.getRequest();
        final String reqInfo = ">> " + httpSvlReq.getMethod() + " " + httpSvlReq.getRequestURI() + " " + httpSvlReq.getProtocol();
        final Signature sign = pjp.getSignature();
        final String reqFunc = ">> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("=== API Controller begin! ===");
        logger.info(reqInfo);
        logger.info(reqFunc);

        // 컨트롤러 수행
        Object ctrResult = null;

        try {
            ctrResult = pjp.proceed();

            if (ctrResult == null) {
                ctrResult = "{}";
            }
        }
        catch (Throwable e) {
            logger.error("Exception!", e);
            ctrResult = "{}";
        }

        // 결과 로깅 및 반환
        final String ctrResultStr = ctrResult.toString();
        logger.info("<< CtrResult: '" + ctrResultStr + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("=== API Controller end! === (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return ctrResultStr;
    }
}