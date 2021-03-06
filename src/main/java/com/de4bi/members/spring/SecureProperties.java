package com.de4bi.members.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import lombok.Getter;

/**
 * 보안을 위해 application.properties등에 기록하지 않고
 * 서버 로컬에 저장된 properties파일을 읽는 클래스입니다.
 */
@Configuration
@PropertySources({
    @PropertySource(value = "file:${user.home}/configs/members/members.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "file:/home/dev4robi/configs/members/members.properties", ignoreResourceNotFound = true)
})
@Getter
public class SecureProperties {

    // spring.datasource
    @Value("${spring.datasource.driver-class-name}")
    private String dataSourceDriverClassName;
    
    @Value("${spring.datasource.url}")
    private String dataSoruceUrl;

    @Value("${spring.datasource.username}")
    private String dataSoruceUserName;

    @Value("${spring.datasource.password}")
    private String dataSorucePassword;

    // custom.secure.keys
    @Value("${custom.secure.keys.member-password-server-salt}")
    private String memberPasswordServerSalt;

    @Value("${custom.secure.keys.member-jwt-secret}")
    private String memberJwtSecret;

    // custom.oauth.google
    @Value("${custom.oauth.google.client-id}")
    private String googleOauthClientId;

    @Value("${custom.oauth.google.client-secret}")
    private String googleOauthClientSecret;

    @Value("${custom.oauth.google.redirection-sign-key}")
    private String googleOauthRedirectionSignKey;
}