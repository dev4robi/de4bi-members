package com.de4bi.members.aop;

import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.de4bi.common.data.ThreadStorage;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.exception.ControllerException;
import com.de4bi.common.exception.MapperException;
import com.de4bi.common.exception.ServiceException;

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

    // 상수
    public static final String CTR_TID      = "CTR_TID";
    public static final String CTR_REQ_TIME = "REQ_TIME";

    /**
     * Controller전/후를 감싸는 AOP입니다. Controller메서드 호출 및 응답, 예외상황을 핸들링합니다.
     * 
     * @param pjp - {@code @Around}의 필수 인자입니다.
     * @return 클라이언트에게 응답할 내용을 담은 String 객체.
     */
    @Around("execution(* com.de4bi.members.controller.api..*.*(..))")
    public String aroundApiController(ProceedingJoinPoint pjp) {
        // 초기화
        final long bgnTime = System.currentTimeMillis();
        final String tid = RandomStringUtils.randomAlphanumeric(16);
        final String oldLayer = MDC.get("layer");
        MDC.put("layer", "CTR");
        MDC.put("tid", tid);
        ThreadStorage.put(CTR_TID, tid); // 스레드 스토리지에 'tid'를 꼭 넣어줘야 합니다
        ThreadStorage.put(CTR_REQ_TIME, bgnTime);

        // 접근 로깅
        final ServletRequestAttributes svlReqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpSvlReq = svlReqAttrs.getRequest();
        final HttpServletResponse httpSvlRes = svlReqAttrs.getResponse();
        final String reqInfo = ">> " + httpSvlReq.getMethod() + " " + httpSvlReq.getRequestURI() + " " + httpSvlReq.getProtocol();
        final Signature sign = pjp.getSignature();
        final String reqFunc = ">> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("=== API Controller begin! ===");
        logger.info(reqInfo);
        logger.info(reqFunc);

        // 컨트롤러 수행
        Object ctrResult = null;

        try {
            // @RequireJWT를 읽는 부분을 추가해야 합니다. @@ 여기부터 시작하면 될 듯?
            // 일단 각 메서드별 어노테이션을 읽어와 보자... @@
            ctrResult = pjp.proceed();

            if (ctrResult == null) {
                ctrResult = "{}";
            }
        }
        catch (ControllerException e) {
            logger.error("ControllerException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = getErrorResultStr(tid, "[CTR] 요청 처리 중 오류가 발생했습니다.");
        }
        catch (ServiceException e) {
            logger.error("ServiceException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = getErrorResultStr(tid, "[SVC] 서비스 처리 중 오류가 발생했습니다.");
        }
        catch (MapperException e) {
            logger.error("MapperException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = getErrorResultStr(tid, "[MPR] DB처리 중 오류가 발생했습니다.");
        }
        catch (ApiException e) {
            // 외부로 사용자 지정 HTTP Status와 오류 메시지 응답
            logger.error("ApiException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(e.getHttpStatus().value());
            ctrResult = getErrorResultStr(tid, e.getMessage());
        }
        catch (Throwable e) {
            logger.error("UnhandledException!", e);
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = getErrorResultStr(tid, "[SYS] 서버 오류가 발생했습니다.");
        }

        // 결과 로깅 및 반환
        final String ctrResultStr = ctrResult.toString();
        logger.info("<< CtrResult: '" + ctrResultStr + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("=== API Controller end! === (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return ctrResultStr;
    }

    /**
     * 클라이언트에게 전송할 오류 결과 문자열을 다음 포멧으로 생성합니다.
     * <p>{"tid":"($tid)","result":false,"message":"($message)"}</p>
     * 
     * @param tid - 컨트롤러에서 생성된 tid.
     * @param message - 응답 메시지.
     * @return 생성된 JSON문자열을 반환합니다.
     */
    private String getErrorResultStr(String tid, String message) {
        return ("{\"tid\":\"" + tid + "\",\"result\":false,\"message\":\"" + message + "\"}");
    }
}