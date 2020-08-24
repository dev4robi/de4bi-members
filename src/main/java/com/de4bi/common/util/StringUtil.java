package com.de4bi.common.util;

public class StringUtil {
    
    /**
     * <p>문자열이 비었는지 검사합니다.</p>
     * @param str : 검사할 문자열.
     * @return str이 빈 문자열인 경우 {@code (str == null || str.length() == 0)} true, 그 외의 경우 false.
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * <p>문자열이 안 비었는지 검사합니다.</p>
     * @param str : 검사할 문자열.
     * @return str이 빈 문자열인 경우 {@code (str == null || str.length() == 0)} false, 그 외의 경우 true.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * <p>문자열을 {@code '}로 감싸서 반환합니다.</p>
     * @param str : 원본 문자열.
     * @return {@code "'" + str + "'"}
      @apiNote {@code quote("Hello") -> 'Hello'}
     */
    public static String quote(String str) {
        return quote(str, "'");
    }

    /**
     * <p>문자열을 {@code quoteStr}로 감싸서 반환합니다.</p>
     * @param str : 원본 문자열.
     * @param quoteStr : 감쌀 문자열.
     * @return {@code quoteStr + str + quoteStr}
     * @apiNote {@code quote("World", "\"") -> "World"}
     */
    public static String quote(String str, String quoteStr) {
        return quoteStr + str + quoteStr;
    }
}