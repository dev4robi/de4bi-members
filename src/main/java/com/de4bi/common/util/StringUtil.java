package com.de4bi.common.util;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public class StringUtil {
    
    /**
     * <p>입력된 date값을 'yyyy-MM-dd HH:mm:ss'포멧으로 변환합니다.</p>
     * @param date - {@code java.util.Date}시간 값.
     * @return 입력된 date를 변환한 {@code yyyy-MM-dd HH:mm:ss}포멧 문자열.
     */
    public static String format(Date date) {
        Objects.requireNonNull(date, "'date' is null!");
        return date.toInstant().truncatedTo(ChronoUnit.SECONDS).toString().replaceAll("[TZ]", " ").trim();
    }

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