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
     * <p>API/Page Controller전~후를 감싸는 AOP입니다. Controller메서드 호출 및 응답, 예외상황을 핸들링합니다.</p>
     * @param pjp : <code>@Around</code>의 필수 인자
     * @return String(API)또는 ModelAndView(Page)
     */
    @Around("execution(* com.de4bi.members.controller..*.*(..))")
    public Object aroundController(ProceedingJoinPoint pjp) {
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
        final boolean isApiCtr = (sign.getReturnType() != ModelAndView.class);
        final String reqFunc = ">> " + sign.getDeclaringTypeName() + "." + sign.getName() + "()";

        logger.info(isApiCtr ? "=== API Controller begin! ===" : "=== Page Controller begin! ===");
        logger.info(reqInfo);
        logger.info(reqFunc);

        final Map<String, Object> ctrMap = new HashMap<>();
        ctrMap.put("tid", tid);
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
            if ((ctrResult = pjp.proceed()) == null) {
                logger.warn("Controller returns null!");
                ctrResult = (isApiCtr ? ApiResult.of(false).toString() : new ModelAndView("error"));
            }
            else if (isApiCtr) {
                final ApiResult<?> tempResult = (ApiResult<?>) ctrResult;
                tempResult.setMessage("?"); // 코드에서 언어에 맞는 메시지 가져오는 작업 수행##
                ctrResult = tempResult;
            }
        }
        catch (ControllerException e) {
            logger.error("ControllerException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            if (isApiCtr) {
                ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("er?");
            }
            else {
                ctrMap.put("message", "컨트롤러 오류가 발생했습니다.");
            }
        }
        catch (ServiceException e) {
            logger.error("ServiceException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            if (isApiCtr) {
                ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("er?");
            }
            else {
                ctrMap.put("message", "서비스 오류가 발생했습니다.");
            }
        }
        catch (MapperException e) {
            logger.error("MapperException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            if (isApiCtr) {
                ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("er?");
            }
            else {
                ctrMap.put("message", "DB오류가 발생했습니다.");
            }
        }
        catch (ApiException e) {
            logger.error("ApiException! IntMsg:{} / ExtMsg:{} / Cause:{}", e.getInternalMsg(), e.getMessage(), e.getCause());
            httpSvlRes.setStatus(e.getHttpStatus().value());
            if (isApiCtr) {
                ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("er?");
            }
            else {
                ctrMap.put("message", e.getMessage());
            }
        }
        catch (Throwable e) {
            logger.error("UnhandledException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            if (isApiCtr) {
                ctrResult = ApiResult.of(false).setCode(ErrorCode.CC0_ERROR).setMessage("er?");
            }
            else {
                ctrMap.put("message", "미정의된 오류가 발생했습니다.");
            }
        }

        // 결과 로깅 및 반환
        final String ctrResultStr = ctrResult.toString();
        logger.info("<< CtrResult: '" + ctrResultStr + "'");
        final long elapsedTime = System.currentTimeMillis() - bgnTime;
        logger.info(isApiCtr ? "=== API Controller end! ===" : "=== Page Controller end! ===" + " (Time: " + elapsedTime + "ms)");
        MDC.put("layer", oldLayer);
        return ctrResult;
    }
}
