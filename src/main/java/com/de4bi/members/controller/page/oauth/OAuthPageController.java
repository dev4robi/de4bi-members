package com.de4bi.members.controller.page.oauth;

import com.de4bi.members.service.oauth.GoogleOAuthService;
import com.de4bi.members.service.oauth.IOAuthSignin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Controller
public class OAuthPageController {
    
    private final GoogleOAuthService googleOauthSvc;

    
}