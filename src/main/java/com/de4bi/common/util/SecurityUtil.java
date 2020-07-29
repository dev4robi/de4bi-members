package com.de4bi.common.util;

import java.util.Objects;

import org.apache.tomcat.util.buf.HexUtils;

public class SecurityUtil {
    
    /**
     * {@code password + salt} SHA256 해싱된 비밀번호를 16진수로 반환합니다.
     * @param password - 해싱되기 전 비밀번호.
     * @param salt - SALT 문자열. (nullable)
     * @return 해싱된 64자리의 16진수 비밀번호를 반환합니다.
     */
    public static String passwordSecureHashing(final String password, final String salt) {
        Objects.requireNonNull(password, "'password' is null!");
        final String saltedPassword = (salt == null ? password : password + salt);
        return HexUtils.toHexString(CipherUtil.hashing(CipherUtil.SHA256, saltedPassword));
    }
}