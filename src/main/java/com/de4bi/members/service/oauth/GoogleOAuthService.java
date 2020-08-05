package com.de4bi.members.service.oauth;

import java.util.Map;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;

import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Google OAuth를 사용한 회원 가입을 수행합니다.
 */
@Service
public class GoogleOAuthService implements IOAuthSignin {

    private static final String OAUTH_API_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    @Builder @Getter
    public static class AuthUrlParam {
        private String clientId;
        private String responseType;
        private String scope;
        private String nonce;
        private String redirectUri;
        private String state;
        private String prompt;
        private String display;
        private String loginHint;
        private String accessType;
        private boolean includeGrantedScopes;
        private String openidRealm;
        private String hd;
    }

    /**
     * <p>클라이언트가 구글 OAuth를 수행하기 위해 호출해야 할 URL을 생성합니다.</p>
     * @param extDataMap : 
     * @return 성공 시, OAuth수행을 위한 URL을 문자열로 반환합니다.
     */
    public ApiResult<String> makeLoginUrlForClient(Map<String, ?> extDataMap) {
        // @@ 여기부터 시작
        return null;
    }

    /**
     * <p>클라이언트에게 전달받은 토큰을 플랫폼으로부터 검증합니다.</p>
     * @param token : 구글로부터 클라이언트에게 내려준 토큰값.
     * @param extDataMap : 플랫폼별 부가적인 파라미터를 맵으로 전달합니다. (nullable)
     * @return 성공 시, 구글로부터 응답받은 문자열을 담은 ApiResult를 반환합니다.
     */
    public ApiResult<String> verifyAuthToken(String token, Map<String, ?> extDataMap) {
        Objects.requireNonNull("'token' is null!");


        return null;
    }
}