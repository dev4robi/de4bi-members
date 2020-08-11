package com.de4bi.members.service.oauth;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.CipherUtil;
import com.de4bi.members.spring.BootApplication;
import com.de4bi.members.spring.SecureProperties;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Google OAuth를 사용한 회원 가입을 수행합니다.
 */
@AllArgsConstructor
@Service
public class GoogleOAuthService implements IOAuthService {

    private static final String OAUTH_RECIRECT_URI  =
        BootApplication.IS_LOCAL_TEST ? "http://localhost:30000/oauth/google" : "https://members.de4bi.com/oauth/google";
    private static final String OAUTH_API_URL       = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String OAUTH_CLIENT_ID     = "497284575180-0ottstk5ehodlic3siv6srf4usietg9v.apps.googleusercontent.com";

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

        rtSb.append(OAUTH_API_URL)
            .append("?client_id=").append(OAUTH_CLIENT_ID)
            .append("&response_type=code")
            .append("&scope=email%20name") // 이메일과 이름 요청
            .append("&nonce=").append(nonce) // 중복요청 방지용 nonce
            .append("&prompt=none")
            .append("&state=").append(state) // 리다이렉션 URL에서 검사할 서명값
            .append("&redirect_uri=").append(OAUTH_RECIRECT_URI);

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
        
        

        

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>리다이렉션 페이지에서 해시서명 검사용으로 사용할 state값을 생성합니다.</p>
     * @param nonce : 리다이렉션 페이지에서 전달받거나, 최초 시작 페이지에서 무작위로 생성된 문자열.
     * @return 생성된 state문자열을 반환합니다.
     */
    private String makeStateForRedirectionSign(String nonce) {
        return new String(
            CipherUtil.hashing(CipherUtil.SHA256, nonce + secureProperties.getGoogleOauthRedirectionSignKey()));
    }
}