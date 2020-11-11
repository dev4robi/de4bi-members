package com.de4bi.members.controller.page;

import java.util.HashMap;
import java.util.Map;

import com.de4bi.common.util.UrlUtil;
import com.de4bi.members.service.oauth.GoogleOAuthService;
import com.de4bi.members.spring.SecureProperties;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class MainPageController {

    private final GoogleOAuthService googleOAuthSvc;

    // 메인&로그인 페이지
    @RequestMapping(value = {"", "/login"})
    public ModelAndView loginPage(
        // 로그인창 종류(page, popup, iframe)
        @RequestParam(required = false, name = "frame_type", defaultValue = "page") String frameType,
        // 로그인 후 이동할 페이지 URL
        @RequestParam(required = false, name = "return_url", defaultValue = "/info") String returnUrl,
        // 로그인 후 이동할 페이지에 전달할 파라미터
        @RequestParam(required = false, name = "return_data") String returnData,
        // 로그인 완료한 경우 전달받은 member_jwt
        @RequestParam(required = false, name = "member_jwt") String memberJwt

    ) {
        final Map<String, String> modelMap = new HashMap<>();
        // Datapart
        modelMap.put("frame_type", frameType);
        modelMap.put("return_url", returnUrl);
        modelMap.put("return_data", returnData);
        // OAuth URL
        final Map<String, Object> rtParamMap = new HashMap<>(modelMap);
        modelMap.put("google_login_url", googleOAuthSvc.makeLoginUrlForAuthCode(UrlUtil.makeUrlParam(rtParamMap, "`"), null).getData());
        modelMap.put("naver_login_url", "#");
        modelMap.put("kakao_login_url", "#");
        modelMap.put("de4bi_login_url", "#");
        return new ModelAndView("login", modelMap);
    }

    // 회원정보 페이지
    @RequestMapping(value = "/info")
    public ModelAndView infoPage() {
        return new ModelAndView("info");
    }

    // 회원가입 페이지
    @RequestMapping(value = "/signup")
    public ModelAndView signupPage() {
        return null;
    }
}