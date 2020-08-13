package com.de4bi.members.service.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.util.CipherUtil;
import com.de4bi.common.util.RestHttpUtil;
import com.de4bi.members.spring.BootApplication;
import com.de4bi.members.spring.SecureProperties;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.buf.HexUtils;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * Google OAuth를 사용한 회원 가입을 수행합니다.
 * 
 * 사용자 구글 로그인 -> 사용자의 code획득 -> code를 서버로 전달 -> google로부터 code검증 -> token및 사용자 정보 획득
 * (브라우저)            (브라우저)           (인증서버)            (구글서버)               (인증서버)
 * 
 */
@AllArgsConstructor
@Service
public class GoogleOAuthService implements IOAuthService {
    // 공개 상수
    public  static final String OAUTH_CODE_RECIRECT_URI  = 
        BootApplication.IS_LOCAL_TEST ? "http://localhost:30000/oauth/google/code" : "https://members.de4bi.com/oauth/google/code";
    public  static final String OAUTH_TOKEN_REDIRECT_URI = 
        BootApplication.IS_LOCAL_TEST ? "http://localhost:30000/oauth/google/token" : "https://members.de4bi.com/oauth/google/token";

    // 내부 상수
    private static final String OAUTH_CODE_URL      = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String OAUTH_TOKEN_URL     = "https://oauth2.googleapis.com/token";

    // 설정
    private final SecureProperties secureProperties;

    /**
     * <p>사용자가 구글 로그인(OAuth2)으로 인증코드(Authorization Code)를 획득하기 위한 URL을 생성합니다.</p>
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, OAuth수행을 위한 URL을 문자열로 반환합니다.
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#authenticationuriparameters
     */
    public ApiResult<String> makeLoginUrlForAuthCode(Object extObj) {
        final StringBuilder rtSb = new StringBuilder(256);
        final String nonce = RandomStringUtils.randomAlphanumeric(32);
        final String state = makeStateForRedirectionSign(nonce);

        rtSb.append(OAUTH_CODE_URL)
            .append("?client_id=").append(secureProperties.getGoogleOauthClientId())
            .append("&response_type=code")
            .append("&scope=email%20profile") // 이메일과 프로필 요청
            .append("&nonce=").append(nonce) // 중복요청 방지용 nonce
            .append("&prompt=consent")
            .append("&state=").append(state) // 리다이렉션 URL에서 검사할 서명값
            .append("&redirect_uri=").append(OAUTH_CODE_RECIRECT_URI);

        return ApiResult.of(true, null, rtSb.toString());
    }

    /**
     * <p>사용자에게 전달받은 인증코드(Authorization Code)를 구글에 검증요청하여 idToken을 획득합니다.</p>
     * @param code : 구글로부터 사용자에게 내려준 인증코드값.
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, 구글로부터 응답받은 문자열을 담은 ApiResult를 반환합니다.
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#validatinganidtoken
     */
    public ApiResult<String> requestIdTokenUsingAuthCode(String code, Object extObj) {
        Objects.requireNonNull(code, "'code' is null!");

        // state 검사 무조건 만들어야 할 듯
        // 다른 사이트에서 만든 코드를 재활용 할지도 모른다...! @@

        // 요청 바디 생성
        final StringBuilder reqBodySb = new StringBuilder(256);
        reqBodySb.append("code=").append(code)
                 .append("&client_id=").append(secureProperties.getGoogleOauthClientId())
                 .append("&client_secret=").append(secureProperties.getGoogleOauthClientSecret())
                 .append("&redirect_uri=").append(OAUTH_TOKEN_REDIRECT_URI)
                 .append("&grant_type=authorization_code");
        String reqBodyStr = reqBodySb.toString(); //null;        
        // try {
        //     reqBodyStr = URLEncoder.encode(reqBodySb.toString(), "utf-8");
        // }
        // catch (UnsupportedEncodingException e) {
        //     throw new IllegalStateException("Server can't support the charset 'utf-8'!", e); // 자바 명세에 따르면 도달할 수 없는 코드
        // }
        
        // 구글로 token요청 전송
        final List<String> resBodyList = new ArrayList<>();
        RestHttpUtil.httpPost(OAUTH_TOKEN_URL, MediaType.APPLICATION_FORM_URLENCODED, null, reqBodyStr, null, resBodyList);

        // 여기서부터 시작 @@
        // 1. 200 OK 아니면 예외 발생시켜버림 (RestHttpUtil)
        // 2. 구글 code전달까지는 되는데   "error": "redirect_uri_mismatch",
        // "error_description": "Bad Request" 응답이 옴...

        final String res = resBodyList.get(0);
        System.out.println("google_response: " + res);

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>리다이렉션 페이지에서 해시서명 검사용으로 사용할 state값을 생성합니다.</p>
     * @param nonce : 리다이렉션 페이지에서 전달받거나, 최초 시작 페이지에서 무작위로 생성된 문자열.
     * @return 생성된 state문자열을 반환합니다.
     */
    private String makeStateForRedirectionSign(String nonce) {
        return HexUtils.toHexString(
            CipherUtil.hashing(CipherUtil.SHA256, nonce + secureProperties.getGoogleOauthRedirectionSignKey()));
    }
}