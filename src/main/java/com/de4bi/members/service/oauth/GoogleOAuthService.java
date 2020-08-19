package com.de4bi.members.service.oauth;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.de4bi.common.data.ApiResult;
import com.de4bi.common.exception.ApiException;
import com.de4bi.common.util.CipherUtil;
import com.de4bi.common.util.JsonUtil;
import com.de4bi.common.util.JwtUtil;
import com.de4bi.common.util.RestHttpUtil;
import com.de4bi.common.util.UserJwtUtil;
import com.de4bi.members.spring.BootApplication;
import com.de4bi.members.spring.SecureProperties;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.buf.HexUtils;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;

/**
 * Google OAuth를 사용한 회원 가입을 수행합니다.
 * 
 * 사용자 구글 로그인 -> 사용자의 code획득 -> code를 서버로 전달 -> google로부터 code검증 -> token및 사용자
 * 정보 획득 (브라우저) (브라우저) (인증서버) (구글서버) (인증서버)
 * 
 */
@AllArgsConstructor
@Service
public class GoogleOAuthService implements IOAuthService {
    // 공개 상수
    public static final String OAUTH_CODE_RECIRECT_URI = BootApplication.IS_LOCAL_TEST
            ? "http://localhost:30000/oauth/google/code"
            : "https://members.de4bi.com/oauth/google/code";
    public static final String OAUTH_TOKEN_REDIRECT_URI = BootApplication.IS_LOCAL_TEST
            ? "http://localhost:30000/oauth/google/token"
            : "https://members.de4bi.com/oauth/google/token";

    // 내부 상수
    private static final String OAUTH_CODE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String OAUTH_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String OAUTH_TOKEN_KEY_URL = "https://www.googleapis.com/oauth2/v3/certs";

    // 내부 변수
    private static PublicKey ID_TOKEN_SIGNING_PUBLIC_KEY;

    // 설정
    private final SecureProperties secureProperties;

    /**
     * <p>
     * 사용자가 구글 로그인(OAuth2)으로 인증코드(Authorization Code)를 획득하기 위한 URL을 생성합니다.
     * </p>
     * 
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, OAuth수행을 위한 URL을 문자열로 반환합니다.
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#authenticationuriparameters
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#sendauthrequest
     */
    public ApiResult<String> makeLoginUrlForAuthCode(Object extObj) {
        final StringBuilder rtSb = new StringBuilder(256);
        final String nonce = RandomStringUtils.randomAlphanumeric(32);
        final String state = makeStateForRedirectionSign(nonce);

        rtSb.append(OAUTH_CODE_URL).append("?client_id=").append(secureProperties.getGoogleOauthClientId())
                .append("&response_type=code").append("&scope=email%20profile") // 이메일과 프로필 요청
                .append("&nonce=").append(nonce) // 중복요청 방지용 nonce
                .append("&prompt=consent").append("&state=").append(state) // 리다이렉션 URL에서 검사할 서명값
                .append("&redirect_uri=").append(OAUTH_CODE_RECIRECT_URI); // Code값을 리다이렉션할 URI

        return ApiResult.of(true, null, rtSb.toString());
    }

    /**
     * <p>
     * 사용자에게 전달받은 인증코드(Authorization Code)를 구글에 검증요청하여 idToken을 획득합니다.
     * </p>
     * 
     * @param code   : 구글로부터 사용자에게 내려준 인증코드값.
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, 구글로부터 응답받은 문자열을 담은 ApiResult를 반환합니다.
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#exchangecode
     */
    public ApiResult<String> requestIdTokenUsingAuthCode(String code, Object extObj) {
        Objects.requireNonNull(code, "'code' is null!");

        // state 검사 무조건 만들어야 할 듯
        // 다른 사이트에서 만든 코드를 재활용 할지도 모른다...! @@

        // 요청 바디 생성
        final StringBuilder reqBodySb = new StringBuilder(256);
        reqBodySb.append("code=").append(code).append("&client_id=").append(secureProperties.getGoogleOauthClientId())
                .append("&client_secret=").append(secureProperties.getGoogleOauthClientSecret())
                .append("&redirect_uri=").append(OAUTH_CODE_RECIRECT_URI) // Code를 획득한 리다이렉션 URI
                .append("&grant_type=authorization_code");

        // 구글로 token요청 전송
        final String reqBodyStr = reqBodySb.toString();
        final List<String> resBodyList = new ArrayList<>();
        RestHttpUtil.httpPost(OAUTH_TOKEN_URL, MediaType.APPLICATION_FORM_URLENCODED, null, reqBodyStr, null,
                resBodyList);

        // 응답 파싱
        final Map<String, Object> googleResMap = JsonUtil.fromJsonStr(resBodyList.get(0));
        final String idToken = googleResMap.get("id_token").toString();
        boolean isFirstTry = true;

        Map<String, Object> idTokenMap = null;
        try {
            // 최초 파싱시 JWT서명 검사용 공개키 생성
            synchronized (this) {
                if (ID_TOKEN_SIGNING_PUBLIC_KEY == null) {
                    updatePublicKeyForIdTokenSigning(idToken);
                    isFirstTry = false;
                }
            }

            // idToken파싱 결과(Claims)를 Map으로 획득
            idTokenMap = JwtUtil.parseJwt(idToken, null, ID_TOKEN_SIGNING_PUBLIC_KEY);
        }
        catch (IllegalArgumentException e) {
            // JWT가 null이거나 길이가 0이거나, SigningKey가 빌더에 등록되지 않은 경우
            throw e;
        }
        catch (UnsupportedJwtException e) {
            // JWT파싱중 오류 발생
            throw new ApiException("토큰 분석에 실패했습니다.", e.getCause());
        }
        catch (MalformedJwtException e) {
            // JWT토큰 포멧이 아닌경우
            throw new ApiException("'올바르지 않은 토큰 포멧입니다.'", e.getCause());
        }
        catch (ExpiredJwtException e) {
            // 토큰 유효기간이 만료된 경우
            throw new ApiException("만료된 토큰입니다. 다시 로그인 해주세요.", e.getCause());
        }
        catch (SignatureException e) {
            // 서명검사 오류가 발생한 경우.
            // 첫 번째 시도에는 키를 새로 갱신 / 두 번째 시도에는 예외 생성.
            synchronized (this) {
                if (isFirstTry == false) {
                    throw new ApiException("변조된 토큰입니다. 다시 로그인 해주세요.", e.getCause());
                }

                updatePublicKeyForIdTokenSigning(idToken);
                isFirstTry = false;
            }
        }
        catch (MissingClaimException e) {
            // jwtRequried의 key값이 Claims에 존재하지 않는 경우
            throw new ApiException("토큰에 필수 정보가 존재하지 않습니다.", e.getCause());
        }
        catch (IncorrectClaimException e) {
            // jwtRequried의 key값에 해당하는 value가 불일치하는 경우
            throw new ApiException("토큰 필수값이 일치하지 않습니다.", e.getCause());
        }

        // 결과 획득
        System.out.println(idTokenMap.toString());

        String email = idTokenMap.get("email");
        String name = idTokenMap.get("name");

        // @@ 여기부터 시작. email,name에 잘 담겨있다.
        // 사실 이 함수는 2개로 분리되어야 맞다. (idToken획득하는 함수와, 그 이후 개인정보 획득 함수)
        // 아래부분 잘라내서 새로운 함수로 이동시키자.

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * 리다이렉션 페이지에서 해시서명 검사용으로 사용할 state값을 생성합니다.
     * </p>
     * 
     * @param nonce : 리다이렉션 페이지에서 전달받거나, 최초 시작 페이지에서 무작위로 생성된 문자열.
     * @return 생성된 state문자열을 반환합니다.
     */
    private String makeStateForRedirectionSign(String nonce) {
        return HexUtils.toHexString(
                CipherUtil.hashing(CipherUtil.SHA256, nonce + secureProperties.getGoogleOauthRedirectionSignKey()));
    }

    /**
     * <p>
     * {@code ID_TOKEN_SIGNING_PUBLIC_KEY}를 갱신합니다.
     * </p>
     * idToken(JWT)의 해시서명 검사를 위한 공개키를 구글로부터 획득합니다. non-thread-safe한 메서드 이므로 동기화 블럭을
     * 사용하여 호출하도록 해야 합니다.
     * 
     * @param idToken : 응답 idToken(JWT)값.
     */
    private void updatePublicKeyForIdTokenSigning(String idToken) {
        Objects.requireNonNull(idToken, "'idToken' is null!");

        // JWT는 {header}.{claims}.{signature}포멧이므로, {header}부분만 뜯어옴
        // idToken 헤더획득 -> Base64디코딩 -> JSON변환 -> kid획득
        final String idTokenHeader = idToken.substring(0, idToken.indexOf("."));
        final String kid = JsonUtil.fromJsonStr(new String(Base64Utils.decodeFromUrlSafeString(idTokenHeader)))
                .get("kid").toString();

        final List<String> resBodyList = new ArrayList<>();
        RestHttpUtil.httpGet(OAUTH_TOKEN_KEY_URL, null, null, null, resBodyList);

        final String resJsonStr = resBodyList.get(0);
        final Map<String, Object> resJsonMap = JsonUtil.fromJsonStr(resJsonStr);

        /*
         * [Note] 응답 포멧 {"keys": [ { "kid": "744f60e9fb515a2a01c11ebeb228712860540711",
         * "e": "AQAB", "use": "sig" "alg": "RS256", "kty": "RSA", "n":
         * "omK-BgTldoGjO0zHDNXELv4756vbdFPcfTqzs21pQkW9kYlos11jFIomZLa9WgtUVfjF1qjPm8J_UGcmyQNoXOqweY6UusEXhb-sLQ4_5o_R1TlrP2X0bmDwJqMa41ZZR2cs0XGP8B9bWMpq-hTwOHLzMgMc0e4Dty7u8vASve_aH6_11FvNDzFu79ixCId8VwxEPdTeWCZXYRQpTQpw0Kh_koXlV39iVvcH2DmuCmXJKoW2PDXOD4Y7wF_R0mYS6df13jBRNrvlBEDMgx6utKRFYDTWeRrTPBnseWY9Kk48mcAuwOucMs8ce2q9cjyFypnoIkaIdz8dumLk8iqjNQ",
         * 
         * }, { "kid": "6bc63e9f18d561b34f5668f88ae27d48876d8073", "e": "AQAB", "use":
         * "sig", "alg": "RS256", "kty": "RSA", "n":
         * "oprIf14gjc4QjI4YUC0COkn4KAjkBeaEYiPm6jo1G9gngKGflmmfsviR8M3rIKs96DzgurM2U1X2TUIDhqBvNHtUONclV6anAR220PcS72l__rCo9tRQxk7pUDQSZxbbi6a0t5w35FyBoF6agPSK3-nEfOk1_vwD1pivo5X7lrvHSu_0lZ-IfaNF-DhErGTeWb2Zu4fOMtadWfRJrTp3UdaWFvHZxkVZLIQGNFeEcKapVpAB2ey8bmzz1rYHx0LA-DWMxhfiBvA81e68S2dD8ukHjDtgzh2lkWJffJ-H7ncF7Sli_RBuWShWl0q0CtIeW5PBkwVCmrktZtINPV7h5Q"
         * } ]}
         */

        String expBase64 = null; // exponent
        String modBase64 = null; // modular
        String kty = null; // key type
        try {
            // [Note] 의도적인 SuppressWarings이다.
            // 응답 포멧은 위 주석과 같고, 성공적으로 파싱했다면 이 try-catch구문에서 예외가 발생할 일은 없다.
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> keysAry = (List<Map<String, Object>>) resJsonMap.get("keys");

            // 키값 획득
            for (Map<String, Object> keyMap : keysAry) {
                Object value = keyMap.get("kid");
                if (value != null && kid.equals(value)) {
                    expBase64 = keyMap.get("e").toString();
                    modBase64 = keyMap.get("n").toString();
                    kty = keyMap.get("kty").toString();
                    break;
                }
            }
        } catch (ClassCastException | NullPointerException e) {
            throw new IllegalStateException("Malformed response from '" + OAUTH_TOKEN_KEY_URL + "'!");
        }

        // 공개키 생성
        final byte expAry[] = Base64Utils.decodeFromUrlSafeString(expBase64);
        final byte modAry[] = Base64Utils.decodeFromUrlSafeString(modBase64);
        final BigInteger exponent = new BigInteger(new String(Hex.encodeHex(expAry)), 16);
        final BigInteger modulus = new BigInteger(new String(Hex.encodeHex(modAry)), 16);

        try {
            final PublicKey pk = KeyFactory.getInstance(kty).generatePublic(new RSAPublicKeySpec(modulus, exponent));
            ID_TOKEN_SIGNING_PUBLIC_KEY = pk;
        }
        catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Fail to generate public key for JWT sign validation!", e);
        }
    }
}