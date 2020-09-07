package com.de4bi.common.util;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.InvalidKeyException;

public class JwtUtil {    

    // [Methods]
    /**
     * <p>서명된 JWT(Jasson Web Token)을 생성합니다.</p>
     * @param jwtClaims : JWT Body.
     * <pre>
     * - ID(jti) : JWT고유 ID
     * - Subject(sub) : 내용 제목
     * - Audience(aud) : 대상자
     * - Issure(iss) : 발행자
     * - IssuedAt(iat) : 발행시간
     * - Expiration(exp) : 만료시간
     * - NotBefore(nbf) : 유효 시작시간</pre>
     * @param signKey : 서명 키.
     * @return 생성된 JWT 문자열.
     * @see https://github.com/jwtk/jjwt
     */
    public static String buildJwt(Map<String, Object> jwtHeader, Map<String, Object> jwtClaims, Key signKey) {
        if (signKey == null) {
            throw new NullPointerException("'signKey' is null!");
        }
        
        // [Header]
        // - setHeader(Map<String, Object>)
        // [Claims]
        // - setClaims(Map<String, Object>)
        // - setId(String) : 'jti'
        // - setSubject(String) : 'sub'
        // - setAudience(String) : 'aud'
        // - setIssuer(String) : 'iss'
        // - setIssuedAt(String) : 'iat'
        // - setExpiration(Date) : 'exp'
        // - setNotBefore(Date) : 'nbf'
        
        final JwtBuilder jwtBuilder = Jwts.builder();
        if (jwtHeader != null) {
            jwtBuilder.setHeader(jwtHeader);
        }

        if (jwtClaims != null) {
            jwtBuilder.setClaims(jwtClaims);
        }
        
        try {
            return jwtBuilder.signWith(signKey).compact();
        }
        catch (InvalidKeyException e) {
            throw new IllegalStateException("Invaild 'signKey'!", e);
        }
    }
    
    /**
     * <p>JWT(Jasson Web Token)를 파싱하여 데이터를 Map으로 반환합니다.</p>  
     * @param jwtStr : JWT 문자열.
     * @param jwtReqClaims : Claims에 필수로 존재해야 하는 key,value 쌍.
     * @param signKey : 서명 키.

     * @return 생성된 JWT 문자열.
     * @see https://github.com/jwtk/jjwt
     */
    public static Map<String, Object> parseJwt(String jwtStr, Map<String, Object> jwtReqClaims, Key signKey) {
        Objects.requireNonNull(jwtStr, "'jwtStr' is null!");
        Objects.requireNonNull(signKey, "'signKey' is null!");
        
        // 파서 생성
        final JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder().setSigningKey(signKey);
        if (jwtReqClaims != null) {
            for (String key : jwtReqClaims.keySet()) {
                jwtParserBuilder.require(key, jwtReqClaims.get(key));
            }
        }

        final JwtParser jwtParser = jwtParserBuilder.build();
        
        // 파싱 및 Claims 추출
        final Jws<Claims> jws = jwtParser.parseClaimsJws(jwtStr);
        final Claims claims = jws.getBody();

        return new HashMap<String, Object>(claims);
    }
}
