package com.de4bi.common.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.de4bi.common.exception.ApiException;

import org.apache.commons.lang3.RandomStringUtils;

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
import lombok.Builder;
import lombok.Data;

public class MemberJwtUtil {

    ////////////////////////////////////////////////////////////////
    // public static
    ////////////////////////////////////////////////////////////////

    public static final String HEADER_KEY_PADDING = "pad";

    ////////////////////////////////////////////////////////////////
    // nested class
    ////////////////////////////////////////////////////////////////

    @Data @Builder
    public static class JwtClaims {

        protected String id;            // 아이디
        protected String subject;       // 제목
        protected String issuer;        // 발급자
        protected String audience;      // 사용자
        protected long issuedAt;        // 발급시간
        protected long expiration;      // 만료시간
        protected long notBefore;       // 시작시간
    }

    ////////////////////////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////////////////////////

    /**
     * UserJwt를 발급합니다.
     * 
     * @param jwtHeaderMap - 헤더에 추가할 데이터입니다.
     * @param jwtClaims - {@code UserJwtUtil.JwtClaims}로 생성할 수 있는 꾸러미입니다. (not null)
     * @param secret - 해시키 보안을 위해 사용할 값입니다. 이 값과 {@code makeSignKey()}을 사용하여 해시키를 생성합니다. (not null, 256bit)
     * @return 발급된 UserJwt문자열을 반환합니다.
     * @throws JwtException JWT발급 중 오류가 발생한 경우.
     */
    public static String issue(final Map<String, Object> jwtHeaderMap, final JwtClaims jwtClaims, final String secret) throws JwtException {
        // 파라미터 검사
        Objects.requireNonNull(jwtClaims, "'jwtClaims' is null!");
        Objects.requireNonNull(jwtClaims.getId(), "'jwtClaims.id' is null!");
        Objects.requireNonNull(secret, "'secret' is null!");

        // Header 생성 (Header내부에는 항상 HEADER_KEY_PADDING값이 존재해야 함, 없다면 생성)
        final Map<String, Object> headerMap = new LinkedHashMap<>();
        if (Objects.nonNull(jwtHeaderMap)) {
            headerMap.putAll(jwtHeaderMap);
        }

        final Object padObj = headerMap.get(HEADER_KEY_PADDING);
        if (Objects.isNull(padObj)) {
            headerMap.put(HEADER_KEY_PADDING, RandomStringUtils.randomAlphanumeric(16));
        }

        // Claims 생성
        final Map<String, Object> claimsMap = new LinkedHashMap<>();
        claimsMap.put(Claims.ID, jwtClaims.getId());
        claimsMap.put(Claims.SUBJECT, jwtClaims.getSubject());
        claimsMap.put(Claims.ISSUER, jwtClaims.getIssuer());
        claimsMap.put(Claims.AUDIENCE, jwtClaims.getAudience());
        claimsMap.put(Claims.ISSUED_AT, jwtClaims.getIssuedAt());
        claimsMap.put(Claims.EXPIRATION, jwtClaims.getExpiration());
        claimsMap.put(Claims.NOT_BEFORE, jwtClaims.getNotBefore());

        // JWT 생성
        String rtJwt = null;
        
        try {
            rtJwt = Jwts.builder().setHeader(headerMap)
                                  .setId(jwtClaims.getId()).setClaims(claimsMap)
                                  .signWith(makeSignKey(secret), SignatureAlgorithm.HS256)
                                  .compact();
        }
        catch (final Exception e) {
            throw new JwtException("Fail to compact Jwt!", e);
        }

        if (rtJwt == null || rtJwt.length() == 0) {
            throw new JwtException("'rtJwt' is null or zero-len! (rtJwt: " + rtJwt + ")");
        }

        return rtJwt;
    }

    /**
     * 발급된 UserJwt를 검증합니다.
     * 
     * @param memberJwt - 검증할 JWT. (not null)
     * @param secret - 검증할 JWT의 해시키 보안값. (not null)
     * @param reqClaims - Claims에 필수적으로 요구되는 값. null일 시 필수값 없음.
     * @return 성공 시 {@code Jws<Claims>}객체, 실패 시 null.
     * @throws JwtException JWT검증 중 포멧, 유효기간, 필수값, 서명등의 오류가 발생한 경우.
     */
    public static Jws<Claims> validate(final String memberJwt, final String secret, final JwtClaims reqClaims) {
        // 파라미터 검사
        Objects.requireNonNull(memberJwt, "'memberJwt' is null!");
        Objects.requireNonNull(secret, "'secret' is null!");

        // 필수Claims를 가진 파서 생성
        JwtParserBuilder builder = Jwts.parserBuilder().setSigningKey(makeSignKey(secret));
        if (Objects.nonNull(reqClaims)) {
            Object tempObj = null;
            builder = Objects.nonNull(tempObj = reqClaims.getId()       ) ? builder.requireId(tempObj.toString()        ) : builder;
            builder = Objects.nonNull(tempObj = reqClaims.getSubject()  ) ? builder.requireSubject(tempObj.toString()   ) : builder;
            builder = Objects.nonNull(tempObj = reqClaims.getIssuer()   ) ? builder.requireIssuer(tempObj.toString()    ) : builder;
            builder = Objects.nonNull(tempObj = reqClaims.getAudience() ) ? builder.requireAudience(tempObj.toString()  ) : builder;
        }
        
        final JwtParser parser = builder.build();
        Jws<Claims> rtClaims = null;
        try {
            rtClaims = parser.parseClaimsJws(memberJwt);
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
            throw new ApiException("변조된 토큰입니다. 다시 로그인 해주세요.", e.getCause());
        }
        catch (MissingClaimException e) {
            // jwtRequried의 key값이 Claims에 존재하지 않는 경우
            throw new ApiException("토큰에 필수 정보가 존재하지 않습니다.", e.getCause());
        }
        catch (IncorrectClaimException e) {
            // jwtRequried의 key값에 해당하는 value가 불일치하는 경우
            throw new ApiException("토큰 필수값이 일치하지 않습니다.", e.getCause());
        }

        return rtClaims;
    }

    ////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////

    /**
     * JWT검증을 위해 사용할 비밀 SignKey를 생성합니다.
     * 키는 32byte의 0x00배열에 {@code secret.getBytes("utf-8")}
     * 을 xor 수행시킨 결과를 사용하여 생성합니다.
     * 
     * @param secret - 서버 내부에서 사용할 기본 secret값입니다. (not null, 256bit)
     * @return 생성된 HMAC-SHA {@code Key}를 반환합니다.
     */
    private static Key makeSignKey(final String secret) {
        // 파라미터 검사
        Objects.requireNonNull(secret, "'secret' is null!");

        // 키 바이트배열 생성
        final byte[] keyByteAry = new byte[32]; // 32byte미만인 경우 WeakKeyException 발생
        byte[] secretByteAry = null;
        
        try {
            // [Note] 공통 Util이기에 발급하는 서버와 검증하는 서버의 기본 charset이
            //        다른 경우 서명검증 오류가 발생할 수 있기에 utf-8로 고정한다
            secretByteAry = secret.getBytes("utf-8");
        }
        catch (final UnsupportedEncodingException e) {
            // 자바 표준 스펙에 의하면 도달할 수 없는 코드
            secretByteAry = secret.getBytes();
        }

        final int iLimit = Math.min(keyByteAry.length, secretByteAry.length);
        for (int i = 0; i < iLimit; ++i) {
            keyByteAry[i] = secretByteAry[i]; // secret copy
        }

        // SignKey 생성
        return Keys.hmacShaKeyFor(keyByteAry);
    }
}