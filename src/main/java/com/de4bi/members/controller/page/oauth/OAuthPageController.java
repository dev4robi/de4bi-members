package com.de4bi.members.controller.page.oauth;

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
        @RequestParam(required = false, name = "code") String code,
        @RequestParam(required = false, name = "state") String state,
        @RequestParam(required = false, name = "error_subtype") String errorSubtype,
        @RequestParam(required = false, name = "error") String error
    ) {
        membersService. googleOAuthService.getMemberInfoWithOAuth(code, null);

        return new ModelAndView();
    }
}