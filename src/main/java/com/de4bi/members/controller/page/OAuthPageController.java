package com.de4bi.members.controller.page;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.de4bi.common.util.StringUtil;
import com.de4bi.common.util.UrlUtil;
import com.de4bi.members.service.MembersService;
import com.de4bi.members.service.oauth.GoogleOAuthService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class OAuthPageController {

    private final GoogleOAuthService googleOAuthService;
    private final MembersService membersService;

    @RequestMapping("/oauth/google/code")
    public ModelAndView googleAuthCodePage(
        @RequestParam(required = true, name = "code"            ) String code,
        @RequestParam(required = true, name = "state"           ) String state,
        @RequestParam(required = false, name = "error_subtype"  ) String errorSubtype,
        @RequestParam(required = false, name = "error"          ) String error
    ) {
        final String memberJwt = membersService.signin(
            googleOAuthService.getMemberInfoWithOAuth(code, state, null).getData(), true).getData();
        final Map<String, Object> rtParamMap = UrlUtil.parseUrlParam(googleOAuthService.parseState(state).getData()[0], "`");
        final StringBuilder paramSb = new StringBuilder(128);
        for (String key : rtParamMap.keySet()) {
            paramSb.append('&').append(StringUtil.toSnakeCase(key)).append('=').append(rtParamMap.getOrDefault(key, "").toString());
        }
        return new ModelAndView("redirect:/login?member_jwt=" + memberJwt + paramSb.toString());
    }
}