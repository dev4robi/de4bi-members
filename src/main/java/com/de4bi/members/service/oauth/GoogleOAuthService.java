package com.de4bi.members.service.oauth;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalTime;
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
import com.de4bi.common.util.StringUtil;
import com.de4bi.members.controller.dto.PostMembersDto;
import com.de4bi.members.data.code.MembersCode;
import com.de4bi.members.spring.BootApplication;
import com.de4bi.members.spring.SecureProperties;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.tomcat.util.buf.HexUtils;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;

/**
 * Google OAuth를 사용한 회원 가입을 수행합니다.
 * 
 * 1. 사용자 Google로그인 (브라우저)
 * 2. 사용자의 code획득 및 서버로 전달 (브라우저->멤버서버)
 * 3. Google로부터 code검증하여 idToken획득 (구글서버->멤버서버)
 * 4. idToken검증 및 사용자 정보 획득 (멤버서버)
 * 5. 가입 이력이 있을 경우 MemberJwt발급, 신규 가입의 경우 DB작업 수행 (멤버서버)
 * 
 */
@AllArgsConstructor
@Service
public class GoogleOAuthService implements IOAuthService {

    ////////////////////////////////////////////////////////////////
    // class fields
    ////////////////////////////////////////////////////////////////

    // 공개 상수
    // 로그인 후 code값을 받을 페이지 주소로, 구글 개발자 콘솔에 등록이 되어있어야 합니다
    public static final String OAUTH_CODE_REDIRECT_URI = BootApplication.IS_LOCAL_TEST
        ? "http://localhost:30000/oauth/google/code"
        : "https://members.de4bi.com/oauth/google/code";

    // 내부 상수
    private static final String OAUTH_CODE_URL      = "https://accounts.google.com/o/oauth2/v2/auth";   // google-login -> code
    private static final String OAUTH_TOKEN_URL     = "https://oauth2.googleapis.com/token";            // code -> idToken
    private static final String OAUTH_TOKEN_KEY_URL = "https://www.googleapis.com/oauth2/v3/certs";     // idToken -> JWT (URL for JWT sign validation)

    // 내부 변수
    private static PublicKey ID_TOKEN_SIGNING_PUBLIC_KEY; // idToken서명 검증용 공개키

    // 설정
    private final SecureProperties secureProperties; // 보안 민감한 값을 담은 프로퍼티

    ////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////

    /**
     * <p>리다이렉션 페이지에서 전달받은 state값을 검증하기 위한 해시서명 값을 생성합니다.</p>
     * @param nonce : 리다이렉션 페이지에서 전달받거나, 최초 시작 페이지에서 무작위로 생성된 문자열.
     * @return 생성된 state문자열을 반환합니다.
     */
    private String makeStateSignForRedirectionPageStateValidation(String nonce) {
        return HexUtils.toHexString(
                CipherUtil.hashing(CipherUtil.SHA256, nonce + secureProperties.getGoogleOauthRedirectionSignKey()));
    }

    /**
     * <p>{@code ID_TOKEN_SIGNING_PUBLIC_KEY}를 갱신합니다.</p>
     * idToken(JWT)의 해시서명 검사를 위한 공개키를 구글로부터 획득합니다.
     * <strong>{@code non-thread-safe}한 메서드</strong> 이므로 동기화 블럭을 사용하여 호출하도록 해야 합니다.
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

    ////////////////////////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////////////////////////

    /**
     * <p>구글에 넘길 state값을 생성합니다.</p>
     * @param returnParam : code페이지에서 되돌려받을 <code>key1=val1`keyN=valN</code>형태로 URLEncoding된 문자열.
     * @param nonce : sign생성을 위한 nonce 문자열.
     * @return Google OAuth2.0 'state'에 사용할 문자열을 반환합니다.
     * @apiNote 포멧은 다음과 같습니다.
     * <p><code>{UTF-8 URLEncoded(key1=val1`key2=val2`keyN=valN)}:::{sign}</code></p>
     */
    public ApiResult<String> makeState(String returnParam, String nonce) {
        if (returnParam != null && returnParam.contains(":::")) {
            throw ApiException.of(null, "The string \":::\" not allowed for URL!");
        }
        Objects.requireNonNull(nonce, "'nonce' is null!");
        return ApiResult.of(true, null, returnParam + ":::" + makeStateSignForRedirectionPageStateValidation(nonce));
    }

    /**
     * <p>state를 <code>returnParam</code>와 <code>sign</code>로 분리합니다.
     * @param state : 전달받은 state값.
     * @return <code>String[0] = returnParam</code>, <code>String[1] = sign</code>을 반환합니다.
     */
    public ApiResult<String[]> parseState(String state) {
        Objects.requireNonNull(state, "'state' is null!");
        return ApiResult.of(true, state.split(":::"));
    }

    /**
     * <p>사용자가 구글 로그인(OAuth2)으로 인증코드(Authorization Code)를 획득하기 위한 URL을 생성합니다.</p>
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, OAuth수행을 위한 URL을 문자열로 반환합니다.
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#authenticationuriparameters
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#sendauthrequest
     */
    @Override public ApiResult<String> makeLoginUrlForAuthCode(String returnParam, Object extObj) {
        final StringBuilder rtSb = new StringBuilder(256);
        final String nonce = RandomStringUtils.randomAlphanumeric(32);
        final String state = makeState(returnParam, nonce).getData(); // {returnParam}:{sign}

        rtSb.append(OAUTH_CODE_URL).append("?client_id=").append(secureProperties.getGoogleOauthClientId())
                .append("&response_type=code").append("&scope=email%20profile") // 이메일과 프로필 요청
                .append("&nonce=").append(nonce) // 중복요청 방지용 nonce
                .append("&prompt=consent").append("&state=").append(state) // 리다이렉션 URL에서 검사할 서명값
                .append("&redirect_uri=").append(OAUTH_CODE_REDIRECT_URI); // Code값을 리다이렉션할 URI

        return ApiResult.of(true, null, rtSb.toString());
    }

    /**
     * <p>사용자에게 전달받은 인증코드(Authorization Code)를 구글에 검증요청하여 idToken을 획득합니다.</p>
     * @param code   : 구글로부터 사용자에게 내려준 인증코드값.
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, 구글로부터 응답받은 문자열(idToken)을 담은 ApiResult를 반환합니다.
     * @see https://developers.google.com/identity/protocols/oauth2/openid-connect#exchangecode
     */
    @Override public ApiResult<String> requestIdTokenUsingAuthCode(String code, Object extObj) {
        Objects.requireNonNull(code, "'code' is null!");

        // 요청 바디 생성
        final StringBuilder reqBodySb = new StringBuilder(256);
        reqBodySb.append("code=").append(code).append("&client_id=").append(secureProperties.getGoogleOauthClientId())
                 .append("&client_secret=").append(secureProperties.getGoogleOauthClientSecret())
                 .append("&redirect_uri=").append(OAUTH_CODE_REDIRECT_URI) // Code를 획득한 리다이렉션 URI
                 .append("&grant_type=authorization_code");

        // 구글로 token요청 전송
        final String reqBodyStr = reqBodySb.toString();
        final List<String> resBodyList = new ArrayList<>();
        RestHttpUtil.httpPost(OAUTH_TOKEN_URL, MediaType.APPLICATION_FORM_URLENCODED, null, reqBodyStr, null, resBodyList);

        // 응답 파싱
        final Map<String, Object> googleResMap = JsonUtil.fromJsonStr(resBodyList.get(0));

        // id_token 반환
        final String idToken = googleResMap.get("id_token").toString();

        return ApiResult.of(true, null, idToken);
    }

    /**
     * <p>구글로부터 받은 idToken을 처리하여 클라이언트의 정보를 획득합니다.</p>
     * @param idToken : 구글로부터 전달받은 토큰값.
     * @param state : 플랫폼에 Code를위한 URL생성 시 넘겨주었던 고유 식별값.
     * @param extObj : 사용하지 않는 추가 파라미터. (nullable)
     * @return 성공 시, {@link ApiResult}<{@link OAuthDto}>를 반환합니다.
     * @apiNote 내부적으로 최적화 병렬 처리를 위해 synchronized구문을 사용합니다.
     */
    @Override public ApiResult<OAuthDto> getMemberInfoFromIdToken(String idToken, String state, Object extObj) {
        Objects.requireNonNull(idToken, "'idToken' is null!");
        Objects.requireNonNull(state, "'state' is null!");

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
            throw ApiException.of("토큰 분석에 실패했습니다.", e.getMessage(), e.getCause());
        }
        catch (MalformedJwtException e) {
            // JWT토큰 포멧이 아닌경우
            throw ApiException.of("올바르지 않은 토큰 포멧입니다.", e.getMessage(), e.getCause());
        }
        catch (ExpiredJwtException e) {
            // 토큰 유효기간이 만료된 경우
            throw ApiException.of("만료된 토큰입니다. 다시 로그인 해주세요.", e.getMessage(), e.getCause());
        }
        catch (SignatureException e) {
            // 서명검사 오류가 발생한 경우.
            // 첫 번째 시도에는 키를 새로 갱신 / 두 번째 시도에는 예외 생성.
            synchronized (this) {
                if (isFirstTry == false) {
                    throw ApiException.of("변조된 토큰입니다. 다시 로그인 해주세요.", e.getMessage(), e.getCause());
                }

                updatePublicKeyForIdTokenSigning(idToken);
                isFirstTry = false;
            }
        }
        catch (MissingClaimException e) {
            // jwtRequried의 key값이 Claims에 존재하지 않는 경우
            throw ApiException.of("토큰에 필수 정보가 존재하지 않습니다.", e.getMessage(), e.getCause());
        }
        catch (IncorrectClaimException e) {
            // jwtRequried의 key값에 해당하는 value가 불일치하는 경우
            throw ApiException.of("토큰 필수값이 일치하지 않습니다.", e.getMessage(), e.getCause());
        }

        // state검사 (DB를 사용했다면 code를 획득하자 마자 할 수 있었겠지만, 별도의 DB사용을 하지 않으므로 이곳에서라도 검사 수행)
        final String resState = makeStateSignForRedirectionPageStateValidation(idTokenMap.getOrDefault("nonce", "").toString());
        state = parseState(state).getData()[1]; // {returnParam}:{sign}에서 {sign}부분을 획득
        if (state.equals(resState) == false) {
            throw ApiException.of("잘못된 접근입니다. 다시 로그인 해주세요.",
                "Invailed 'state'! (resState: " + resState + ", state: " + state + ")");
        }

        final String email = idTokenMap.getOrDefault("email", "").toString();
        final String name = idTokenMap.getOrDefault("name", "").toString();

        if (StringUtil.isEmpty(email)) {
            throw new IllegalStateException("Fail to get 'email' from google OAuth!");
        }

        if (StringUtil.isEmpty(name)) {
            throw new IllegalStateException("Fail to get 'name' from google OAuth!");
        }

        return ApiResult.of(true, OAuthDto.builder().email(email).name(name).build());
    }

    /**
     * <p>구글 OAuth를 사용하여 회원 정보를 획득합니다.</p>
     * <strong>※ 일반적인 경우 이 메서드만 사용하면 됩니다.</strong>
     * @param code : 플랫폼으로부터 클라이언트에게 내려준 인증코드값.
     * @param state : 플랫폼에 Code를위한 URL생성 시 넘겨주었던 고유 식별값.
     * @param extObj : 플랫폼 종속 파라미터를 전달할 객체입니다. (nullable)
     * @return 성공 시, {@link ApiResult}<{@link PostMembersDto}>를 반환합니다.
     * @apiNote 내부적으로 {@link IOAuthService}인터페이스의 메서드인
     * {@code requestIdTokenUsingAuthCode()}, {@code getMemberInfoFromIdToken}를 호출합니다.
     */
    @Override public ApiResult<PostMembersDto> getMemberInfoWithOAuth(String code, String state, Object extObj) {
        final String idToken = requestIdTokenUsingAuthCode(code, null).getData();
        final OAuthDto oauthDto = getMemberInfoFromIdToken(idToken, state, extObj).getData();
        // 임시 닉네임 생성: {인증기관코드}#{SUBSTR(MD5({이메일}+{이름}+{현재시간}),8)} -> cf.) GOOGLE#af32a1a0
        final String temporalNickname = MembersCode.MEMBERS_AUTHAGENCY_GOOOLE.getValue() + "#" +
            Hex.encodeHexString(CipherUtil.hashing(CipherUtil.MD5,
                oauthDto.getEmail() + oauthDto.getName() + LocalTime.now().toString())).substring(0, 8);
        final PostMembersDto rtPostMembersDto = PostMembersDto.builder()
            .id(oauthDto.getEmail())
            .password(null)
            .nickname(temporalNickname)
            .name(oauthDto.getName())
            .authAgency(MembersCode.MEMBERS_AUTHAGENCY_GOOOLE.getSeq())
            .build();
        return ApiResult.of(true, rtPostMembersDto);
    }
}
