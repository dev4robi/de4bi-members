package com.de4bi.members.controller.page;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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
        final Map<String, Object> rtParamMap = UrlUtil.parseUrlParam(state.substring(state.lastIndexOf(":" + 1)), "`");

        // @@여기부터 시작
        // state생성시 :가 들어가는데, 위 코드에서 분리하는건 좀 무리수같다...
        // getMemberInfoWithOauth에서 리턴파람을 같이 응답주는건 어떨까??

        final String frameType = rtParamMap.getOrDefault("frame_type", "").toString();
        final String urlParam = ("?member_jwt=" + memberJwt + "&frame_type=" +frameType + "&return_url=");
        return new ModelAndView("redirect:/login" + urlParam);
    }
}