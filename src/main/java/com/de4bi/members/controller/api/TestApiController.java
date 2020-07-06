package com.de4bi.members.controller.api;

import java.util.HashMap;

import com.de4bi.common.util.UserJwtUtil;
import com.de4bi.common.util.UserJwtUtil.JwtClaims;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class TestApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestApiController.class);

    @GetMapping("/test")
    public String getTest() {
        final long curTime = System.currentTimeMillis();
        final String secret = "e7r6b97ia3956f2459sai3e8iuvb47ha";
        final String userJwt = UserJwtUtil.issue(new HashMap<>(), JwtClaims.builder()
            .id("robi9202@gamil.com").subject("de4bi-user-jwt").issuer("de4bi-members").audience("de4bi-bgz")
            .issuedAt(curTime).expiration(curTime + 60000L).notBefore(curTime).build(), secret);

        if (UserJwtUtil.validate(userJwt, secret)) {
            return "valid:" + userJwt;
        }
        else {
            return "invaild:" + userJwt;
        }
}
        
}