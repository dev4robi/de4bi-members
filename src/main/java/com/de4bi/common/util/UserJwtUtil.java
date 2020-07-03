package com.de4bi.common.util;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@PropertySource("application.properties")
public class UserJwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(UserJwtUtil.class);

    ////////////////////////////////////////////////////////////////
    // public methods
    ////////////////////////////////////////////////////////////////

    /**
     * 
     * @return
     */
    public static String issue(
        String id, String subject, String issuer, String audience,
        long issuedAt, long expiration, long notBefore
    ) {
        // 파라미터 검사
        Objects.requireNonNull(id, "'id' is null!");

        // Header 생성
        final Map<String, Object> headerMap = new LinkedHashMap<>();
        final String padding = RandomStringUtils.randomAlphanumeric(16);
        headerMap.put("pad", padding);

        // Claims 생성
        final Map<String, Object> claimsMap = new LinkedHashMap<>();
        claimsMap.put(Claims.ID, id);
        claimsMap.put(Claims.SUBJECT, subject);
        claimsMap.put(Claims.ISSUER, issuer);
        claimsMap.put(Claims.AUDIENCE, audience);
        claimsMap.put(Claims.ISSUED_AT, issuedAt);
        claimsMap.put(Claims.EXPIRATION, expiration);
        claimsMap.put(Claims.NOT_BEFORE, notBefore);

        // JWT 생성
        String rtJwt = null;
        try {
            rtJwt = Jwts.builder().setHeader(headerMap).setClaims(claimsMap)
                                  .signWith(makeSignKey(id, padding), SignatureAlgorithm.ES256)
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
    public static boolean validate() {
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // private methods
    ////////////////////////////////////////////////////////////////

    /**
     * JWT검증을 위해 사용할 비밀 SignKey를 생성합니다.
     * 키는 32byte의 0x00배열에 {@code id.getBytes("utf-8") xor
     * padding.getBytes("utf-8")}을 할당한 결과를 사용하여 생성합니다.
     * 
     * @param id - 발급자 id입니다.
     * @param padding - 키 보안성 향상을 위한 무작위 문자열입니다.
     * @return 생성된 HMAC-SHA {@code Key}를 반환합니다.
     */
    private static Key makeSignKey(String id, String padding) {
        // 파라미터 검사
        Objects.requireNonNull(id, "'id' is null!");

        // 키 바이트배열 생성
        byte[] keyByteAry = new byte[32]; // 32byte미만인 경우 WeakKeyException 발생
        byte[] idByteAry = null;
        byte[] padByteAry = null;
        
        try {
            // [Note] 공통 Util이기에 발급하는 서버와 검증하는 서버의 기본 charset이
            //        다른 경우 서명검증 오류가 발생할 수 있기에 utf-8로 고정한다
            idByteAry = id.getBytes("utf-8");
            padByteAry = padding.getBytes("utf-8");
        }
        catch (UnsupportedEncodingException e) {
            // 자바 표준 스펙에 의하면 도달할 수 없는 코드
            idByteAry = id.getBytes();
            padByteAry = padding.getBytes();
        }

        final int iLimit = Math.min(keyByteAry.length, idByteAry.length);
        for (int i = 0; i < iLimit; ++i) {
            keyByteAry[i] = idByteAry[i];
        }

        final int jLimit = Math.min(keyByteAry.length, padByteAry.length);
        for (int j = 0; j < jLimit; ++j) {
            keyByteAry[j] ^= padByteAry[j];
        }

        // SignKey 생성
        return Keys.hmacShaKeyFor(keyByteAry);
    }
}