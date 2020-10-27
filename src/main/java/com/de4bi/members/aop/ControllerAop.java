package com.de4bi.members.aop;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.de4bi.members.data.code.ResponseCode;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.data.dao.MembersDao;
import com.de4bi.members.manager.CodeMsgManager;
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

    // 매니저
    private CodeMsgManager codeMsgManager;

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
            boolean doProcess = true;

            // 사용자 정의 어노테이션 검사 수행
            final boolean reqManagerJwt = method.getAnnotation(RequireManagerJwt.class) != null ? true : false;
            if (reqManagerJwt || method.getAnnotation(RequireMemberJwt.class) != null) {
                final String memberJwt = httpSvlReq.getHeader("member_jwt");
                final ApiResult<MembersDao> valRst = membersService.validateMemberJwt(memberJwt, null);
                if (valRst.getResult() == false) {
                    doProcess = false;
                }
                
                if (reqManagerJwt && MembersUtil.checkMemberAuthority(valRst.getData(), MembersCode.MEMBERS_AUTHORITY_MANAGER).getResult() == false) {
                    doProcess = false;
                }

                if (doProcess) {
                    ThreadStorage.put(TSKEY_JWT_MEMBERS_DAO, valRst.getData());
                }
            }

            // 컨트롤러 수행
            if (doProcess) {
                ctrResult = pjp.proceed();
            }
        }
        // catch (ControllerException e) {
        //     logger.error("ControllerException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
        //     httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        //     if (isApiCtr) {
        //         final ApiResult<?> tempResult = ApiResult.of(false).setCode(ResponseCode.A_FAIL);
        //         tempResult.setMessage(codeMsgManager.getMsg(tempResult.getCode(), null));
        //         ctrResult = tempResult;
        //     }
        //     else {
        //         ctrMap.put("message", "컨트롤러 오류가 발생했습니다.");
        //     }
        // }
        // catch (ServiceException e) {
        //     logger.error("ServiceException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
        //     httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        //     if (isApiCtr) {
        //         final ApiResult<?> tempResult = ApiResult.of(false).setCode(ResponseCode.A_FAIL);
        //         tempResult.setMessage(codeMsgManager.getMsg(tempResult.getCode(), null));
        //         ctrResult = tempResult;
        //     }
        //     else {
        //         ctrMap.put("message", "서비스 오류가 발생했습니다.");
        //     }
        // }
        // catch (MapperException e) {
        //     logger.error("MapperException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
        //     httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        //     if (isApiCtr) {
        //         final ApiResult<?> tempResult = ApiResult.of(false).setCode(ResponseCode.A_FAIL);
        //         tempResult.setMessage(codeMsgManager.getMsg(tempResult.getCode(), null));
        //         ctrResult = tempResult;
        //     }
        //     else {
        //         ctrMap.put("message", "DB오류가 발생했습니다.");
        //     }
        // }
        // 기존 구조는 ApiResult false를 반환하는 것 없이, ApiException을 마구 던지는 식으로 개발하여
        // 지금의 구조와는 조금 어울리지 않는 문제점이 있다.
        // 따라서, AoP구조의 변경이 불가피하게 필요한 상황.
        // 지금 ApiException은, 특정 상황에서 긴급하게 탈출하는 용도로 사용하게 될듯 한데,
        // 이를 반영하여 작업을 해야 할 듯.
        // 페이지를 처리하는 aop와 api를 처리하는 aop가 하나로 통일됨에 따라 생기는 변수도
        // 관리해 줘야 한다..
        // api를 응답해야 할 경우에는 apiException이 발생하더라도 error 페이지로 안내되는것이 아니라
        // 해당 내용을 코드변환하여 출력해 주는것이 맞으며,
        // 페이지를 반환해야 하는 경우에는 apiException이 발생한 경우 error페이지에 코드변환된 메시지를 출력해 주어야 할 것이다.
        // 그 외 상황에 대해서도 한번쯤 정리가 필요하다. 여기부터 시작해 보자. @@
        catch (ApiException e) {
            logger.error("ApiException! IntMsg:{} / ExtMsg:{} / Cause:{}", e.getInternalMsg(), e.getMessage(), e.getCause());
            httpSvlRes.setStatus(e.getHttpStatus().value());
            if (isApiCtr) {
                final ApiResult<?> tempResult = ApiResult.of(false).setCode(ResponseCode.A_FAIL);
                tempResult.setMessage(codeMsgManager.getMsg(tempResult.getCode(), null));
                ctrResult = tempResult;
            }
            else {
                ctrMap.put("message", codeMsgManager.getMsg(e.getMessage(), null));
            }
        }
        catch (Throwable e) {
            logger.error("UnhandledException! Msg:{} / Cause:{}", e.getMessage(), e.getCause());
            httpSvlRes.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            if (isApiCtr) {
                final ApiResult<?> tempResult = ApiResult.of(false).setCode(ResponseCode.A_FAIL);
                tempResult.setMessage(codeMsgManager.getMsg(tempResult.getCode(), null));
                ctrResult = tempResult;
            }
            else {
                ctrMap.put("message", "미정의된 오류가 발생했습니다.");
            }
        }

        // 수행결과 재조립
        if (ctrResult == null) {
            logger.warn("Controller returns null!");
            ctrResult = (isApiCtr ? ApiResult.of(false).toString() : new ModelAndView("error"));
        }
        else if (isApiCtr) {
            final ApiResult<?> tempResult = (ApiResult<?>) ctrResult;
            final String tempResCode = tempResult.getCode();
            if (tempResCode == null) tempResult.setCode(tempResult.getResult() ? ResponseCode.A_SUCCESS : ResponseCode.A_FAIL);
            tempResult.setMessage(codeMsgManager.getMsg(tempResult.getCode(), tempResult.getMsgParamList()));
            ctrResult = tempResult;
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
