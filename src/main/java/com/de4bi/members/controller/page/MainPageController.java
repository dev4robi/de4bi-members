package com.de4bi.members.controller.page;

import java.util.HashMap;
import java.util.Map;

import com.de4bi.members.service.oauth.GoogleOAuthService;

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
    public ModelAndView loginPage() {
        final Map<String, String> modelMap = new HashMap<>();
        modelMap.put("google_login_url", googleOAuthSvc.makeLoginUrlForAuthCode(null).getData());
        modelMap.put("naver_login_url", "#");
        modelMap.put("kakao_login_url", "#");
        modelMap.put("de4bi_login_url", "#");
        return new ModelAndView("login", modelMap);
    }

    // 임시 페이지
    @RequestMapping(value = "/replace")
    public ModelAndView replacePage(@RequestParam Map<String, Object> paramMap) {
        return new ModelAndView("replace", paramMap);
    }

    // 회원가입 페이지
    @RequestMapping(value = "/signup")
    public ModelAndView signupPage() {
        return null;
    }
}