package com.de4bi.common.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Builder;
import lombok.Data;

public class UserJwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(UserJwtUtil.class);

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
     */
    public static String issue(Map<String, Object> jwtHeaderMap, JwtClaims jwtClaims, String secret) {
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

        final String padding = headerMap.get(HEADER_KEY_PADDING).toString();

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
                                  .signWith(makeSignKey(secret, jwtClaims.getId(), padding), SignatureAlgorithm.HS256)
                                  .compact();
        }
        catch (Exception e) {
            throw new IllegalStateException("Fail to compact Jwt!", e);
        }

        if (rtJwt == null || rtJwt.length() == 0) {
            throw new IllegalStateException("'rtJwt' is null or zero-len! (rtJwt: " + rtJwt + ")");
        }

        return rtJwt;
    }

    /**
     * 
     * @return
     */
    public static boolean validate(String userJwt, String secret) {
        JwtParser parser = Jwts.parserBuilder().build();

        // 지금의 키 방식은 서명 검사가 문제가 된다...
        // 어떻게 해야할지 다시 한번 고민해 보자. @@
        
        io.jsonwebtoken.Jws<Claims> c = null;
        try {
            c = parser.parseClaimsJws(userJwt);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        logger.info(c.toString());

        return true;
    }

    ////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////

    /**
     * JWT검증을 위해 사용할 비밀 SignKey를 생성합니다.
     * 키는 32byte의 0x00배열에 {@code secret.getBytes("utf-8"), id.getBytes("utf-8")와 
     * padding.getBytes("utf-8")}을 적절히 혼합한 결과를 사용하여 생성합니다.
     * 
     * @param secret - 서버 내부에서 사용할 기본 secret값입니다. (not null, 256bit)
     * @param id - 발급자 id입니다. (not null)
     * @param padding - 키 보안성 향상을 위한 무작위 문자열입니다. (not null)
     * @return 생성된 HMAC-SHA {@code Key}를 반환합니다.
     */
    private static Key makeSignKey(String secret, String id, String padding) {
        // 파라미터 검사
        Objects.requireNonNull(id, "'id' is null!");
        Objects.requireNonNull(padding, "'padding' is null!");
        Objects.requireNonNull(secret, "'secret' is null!");

        // 키 바이트배열 생성
        byte[] keyByteAry = new byte[32]; // 32byte미만인 경우 WeakKeyException 발생
        byte[] secretByteAry = null;
        byte[] idByteAry = null;
        byte[] padByteAry = null;
        
        try {
            // [Note] 공통 Util이기에 발급하는 서버와 검증하는 서버의 기본 charset이
            //        다른 경우 서명검증 오류가 발생할 수 있기에 utf-8로 고정한다
            secretByteAry = secret.getBytes("utf-8");
            idByteAry = id.getBytes("utf-8");
            padByteAry = padding.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            // 자바 표준 스펙에 의하면 도달할 수 없는 코드
            secretByteAry = secret.getBytes();
            idByteAry = id.getBytes();
            padByteAry = padding.getBytes();
        }

        final int iLimit = Math.min(keyByteAry.length, secretByteAry.length);
        for (int i = 0; i < iLimit; ++i) {
            keyByteAry[i] = secretByteAry[i]; // secret copy
        }

        final int jLimit = Math.min(keyByteAry.length, idByteAry.length);
        for (int j = 0; j < jLimit; ++j) {
            keyByteAry[j] ^= idByteAry[j]; // id xor
        }

        final int kLimit = Math.max(keyByteAry.length, padByteAry.length);
        for (int k = 0; k < kLimit; ++k) {
            keyByteAry[k] ^= padByteAry[k % padByteAry.length]; // pad xor (x2)
        }

        // SignKey 생성
        return Keys.hmacShaKeyFor(keyByteAry);
    }
}