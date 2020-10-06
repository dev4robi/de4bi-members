package com.de4bi.members.controller.page;

import java.util.Map;

import com.de4bi.common.util.StringUtil;
import com.de4bi.common.util.UrlUtil;
import com.de4bi.members.controller.dto.SocialSigninMembersDto;
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
        // 구글로부터 전달받은 정보 파싱
        final Map<String, Object> rtParamMap = UrlUtil.parseUrlParam(googleOAuthService.parseState(state).getData()[0], "`");
        final SocialSigninMembersDto newMembersDto = googleOAuthService.getMemberInfoWithOAuth(code, state, null).getData();
        
        // 회원가입 수행
        membersService.socialSignin(newMembersDto).getResult();

        // 자동 로그인 수행
        final boolean isKeepLoggedIn = rtParamMap.getOrDefault("keep_logged_in", "").toString().equals("true") ? true : false;
        final String memberJwt = membersService.socialLogin(newMembersDto.getId(), null, isKeepLoggedIn).getData();

        // 로그인페이지로 파라미터 전달하면서 이동
        final StringBuilder paramSb = new StringBuilder(128);
        for (String key : rtParamMap.keySet()) {
            paramSb.append('&').append(StringUtil.toSnakeCase(key)).append('=').append(rtParamMap.getOrDefault(key, "").toString());
        }
        return new ModelAndView("redirect:/login?member_jwt=" + memberJwt + paramSb.toString());
    }
}