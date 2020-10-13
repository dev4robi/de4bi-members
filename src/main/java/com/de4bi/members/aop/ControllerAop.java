package com.de4bi.members.aop;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.de4bi.common.annotation.RequireManagerJwt;
import com.de4bi.common.annotation.RequireMemberJwt;
import com.de4bi.common.data.ApiResult;
import com.de4bi.common.data.ThreadStorage;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.exception.ControllerException;
import com.de4bi.common.exception.MapperException;
import com.de4bi.common.exception.ServiceException;
import com.de4bi.members.data.code.ErrorCode;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.service.MembersService;
import com.de4bi.members.util.MembersUtil;

import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Aspect
@Component
public class ControllerAop {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAop.class);

    // 상수
    public static final String TSKEY_CTR_REQ_TIME =
        "TS_REQ_TIME"; // ThreadStorage에서 컨트롤러 요청 시간을 저장하기 위한 키 값
    public static final String TSKEY_JWT_MEMBERS_DAO =
        "TS_JWT_MEMBERS_DAO"; // ThreadStorage에서 인증된 MemberJwt의 MemberDao를 저장하기 위한 키 값

    // 서비스
    private MembersService membersService;

    /**
     * <p>Page Controller전/후를 감싸는 AOP입니다. Controller메서드 호출 및 응답, 예외상황을 핸들링합니다.</p>
     * @param pjp : <code>@Around</code>의 필수 인자
     * @return ...
     */
    @Around("execution(* com.de4bi.members.controller.page..*.*(..))")
    public Object aroundPageController(ProceedingJoinPoint pjp) {
        // 초기화
        final long bgnTime = System.currentTimeMillis();
        final String tid = RandomStringUtils.randomAlphanumeric(16);
        final String oldLayer = MDC.get("layer");
        MDC.put("layer", "CTR");
        MDC.put("tid", tid);
        ThreadStorage.put(ApiResult.KEY_TID, tid); // 스레드 스토리지에 'tid'를 꼭 넣어줘야 합니다
        ThreadStorage.put(TSKEY_CTR_REQ_TIME, bgnTime);

        // 접근 로깅
        final ServletRequestAttributes svlReqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpSvlReq = svlReqAttrs.getRequest();
        final HttpServletResponse httpSvlRes = svlReqAttrs.getResponse();
        final String reqInfo = ">> " + httpSvlReq.getMethod() + " " + httpSvlReq.getRequestURI() + " " + httpSvlReq.getProtocol();
        final MethodSignature sign = (MethodSignature) pjp.getSignature();
        final Method method = sign.getMethod();
        final String reqFunc = ">> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("=== Page Controller begin! ===");
        logger.info(reqInfo);
        logger.info(reqFunc);

        boolean errorPageFlag = false;
        Object ctrResult = null;
        final Map<String, Object> ctrMap = new HashMap<>();
        try {
            // 사용자 정의 어노테이션 검사 수행
            final boolean reqManagerJwt = method.getAnnotation(RequireManagerJwt.class) != null ? true : false;
            if (reqManagerJwt || method.getAnnotation(RequireMemberJwt.class) != null) {
                final String memberJwt = httpSvlReq.getHeader("member_jwt");
                final ApiResult<MembersDao> valRst = membersService.validateMemberJwt(memberJwt, null);
                if (valRst.getResult() == false) {
                    throw ApiException.of("로그인이 필요합니다. (" + ErrorCode.MA0_JWT_VALIDATION_FAIL + ")", valRst.getMessage());
                }
                
                if (reqManagerJwt && MembersUtil.checkMemberAuthority(valRst.getData(), MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult() == false) {
                    throw ApiException.of("해당 기능을 수행할 권한이 없습니다. (" + ErrorCode.MG0_NO_PERMISSIONS + ")", valRst.getMessage());
                }

                ThreadStorage.put(TSKEY_JWT_MEMBERS_DAO, valRst.getData());
            }

            // 컨트롤러 수행
            ctrResult = (ModelAndView) pjp.proceed();

            if (ctrResult == null) {
                errorPageFlag = true;
            }
        }
        catch (ControllerException e) {
            errorPageFlag = true;
            logger.error("ControllerException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrMap.put("msg", "컨트롤러 오류가 발생했습니다.");
        }
        catch (ServiceException e) {
            errorPageFlag = true;
            logger.error("ServiceException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrMap.put("msg", "서비스 오류가 발생했습니다.");
        }
        catch (MapperException e) {
            errorPageFlag = true;
            logger.error("MapperException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrMap.put("msg", "DB오류가 발생했습니다.");
        }
        catch (ApiException e) {
            // 외부로 사용자 지정 HTTP Status와 오류 메시지 응답
            errorPageFlag = true;
            logger.error("ApiException! IntMsg:{} / ExtMsg:{} / Cause:{}", e.getInternalMsg(), e.getMessage(), e.getCause());
            httpSvlRes.setStatus(e.getHttpStatus().value());
            ctrMap.put("msg", e.getMessage());
        }
        catch (Throwable e) {
            logger.error("UnhandledException!", e);
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrMap.put("msg", "요청 처리중 미정의된 오류가 발생했습니다.");
            errorPageFlag = true;
        }

        if (errorPageFlag) {
            ctrMap.put("tid", tid);
            ctrResult = new ModelAndView("error").addAllObjects(ctrMap);
        }

        // 결과 로깅 및 반환
        final String ctrResultStr = ctrResult.toString();
        logger.info("<< CtrResult: '" + ctrResultStr + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info("=== Page Controller end! === (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return ctrResult;
    }

    /**
     * <p>API Controller전/후를 감싸는 AOP입니다. Controller메서드 호출 및 응답, 예외상황을 핸들링합니다.</p>
     * @param pjp : {@code @Around}의 필수 인자입니다.
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
        ThreadStorage.put(ApiResult.KEY_TID, tid); // 스레드 스토리지에 'tid'를 꼭 넣어줘야 합니다
        ThreadStorage.put(TSKEY_CTR_REQ_TIME, bgnTime);

        // 접근 로깅
        final ServletRequestAttributes svlReqAttrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        final HttpServletRequest httpSvlReq = svlReqAttrs.getRequest();
        final HttpServletResponse httpSvlRes = svlReqAttrs.getResponse();
        final String reqInfo = ">> " + httpSvlReq.getMethod() + " " + httpSvlReq.getRequestURI() + " " + httpSvlReq.getProtocol();
        final MethodSignature sign = (MethodSignature) pjp.getSignature();
        final Method method = sign.getMethod();
        final String reqFunc = ">> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info("=== API Controller begin! ===");
        logger.info(reqInfo);
        logger.info(reqFunc);

        Object ctrResult = null;
        try {
            // 사용자 정의 어노테이션 검사 수행
            final boolean reqManagerJwt = method.getAnnotation(RequireManagerJwt.class) != null ? true : false;
            if (reqManagerJwt || method.getAnnotation(RequireMemberJwt.class) != null) {
                final String memberJwt = httpSvlReq.getHeader("member_jwt");
                final ApiResult<MembersDao> valRst = membersService.validateMemberJwt(memberJwt, null);
                if (valRst.getResult() == false) {
                    throw ApiException.of("로그인이 필요합니다. (" + ErrorCode.MA0_JWT_VALIDATION_FAIL + ")", valRst.getMessage());
                }
                
                if (reqManagerJwt && MembersUtil.checkMemberAuthority(valRst.getData(), MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult() == false) {
                    throw ApiException.of("해당 기능을 수행할 권한이 없습니다. (" + ErrorCode.MG0_NO_PERMISSIONS + ")", valRst.getMessage());
                }

                ThreadStorage.put(TSKEY_JWT_MEMBERS_DAO, valRst.getData());
            }

            // 컨트롤러 수행
            ctrResult = pjp.proceed();

            if (ctrResult == null) {
                ctrResult = "{}";
            }
        }
        catch (ControllerException e) {
            logger.error("ControllerException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("[CTR] 요청 처리 중 오류가 발생했습니다.").toString();
        }
        catch (ServiceException e) {
            logger.error("ServiceException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("[SVC] 서비스 처리 중 오류가 발생했습니다.").toString();
        }
        catch (MapperException e) {
            logger.error("MapperException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("[MPR] DB 처리 중 오류가 발생했습니다.").toString();
        }
        catch (ApiException e) {
            // 외부로 사용자 지정 HTTP Status와 오류 메시지 응답
            logger.error("ApiException! IntMsg:{} / ExtMsg:{} / Cause:{}", e.getInternalMsg(), e.getExternalMsg(), e.getCause());
            httpSvlRes.setStatus(e.getHttpStatus().value());
            ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage(e.getExternalMsg()).toString();
        }
        catch (Throwable e) {
            logger.error("UnhandledException! Cause:{}", e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("[SYS] 서버 오류가 발생했습니다.").toString();
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