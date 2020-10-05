package com.de4bi.common.util;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public class StringUtil {
    
    /**
     * <p>입력된 date값을 'yyyy-MM-dd HH:mm:ss'포멧으로 변환합니다.</p>
     * @param date - {@code java.util.Date}시간 값
     * @return 입력된 date를 변환한 {@code yyyy-MM-dd HH:mm:ss}포멧 문자열.
     */
    public static String format(Date date) {
        Objects.requireNonNull(date, "'date' is null!");
        return date.toInstant().truncatedTo(ChronoUnit.SECONDS).toString().replaceAll("[TZ]", " ").trim();
    }

    /**
     * <p>문자열이 비었는지 검사합니다.</p>
     * @param str : 검사할 문자열
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
     * @param str : 검사할 문자열
     * @return str이 빈 문자열인 경우 {@code (str == null || str.length() == 0)} false, 그 외의 경우 true.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * <p>문자열을 {@code '}로 감싸서 반환합니다.</p>
     * @param str : 원본 문자열
     * @return {@code "'" + str + "'"}
      @apiNote {@code quote("Hello") -> 'Hello'}
     */
    public static String quote(String str) {
        return quote(str, "'");
    }

    /**
     * <p>문자열을 {@code quoteStr}로 감싸서 반환합니다.</p>
     * @param str : 원본 문자열
     * @param quoteStr : 감쌀 문자열
     * @return {@code quoteStr + str + quoteStr}
     * @apiNote {@code quote("World", "\"") -> "World"}
     */
    public static String quote(String str, String quoteStr) {
        return quoteStr + str + quoteStr;
    }

    /**
     * <p> 문자열을 <code>()</code>로 감싸서 반환합니다.
     * @param str : 감쌀 문자열
     * @return <code>"(" + str + ")"</code>
     * @apiNote <code> wrap("Hello: World!"); -> "(Hello: World!)"</code>
     */
    public static String wrap(String str) {
        return "(" + str + ")";
    }

    /**
     * <p> 문자열을 <code>(),[],{},<></code>중 하나로 감싸서 반환합니다.</p>
     * @param str : 감쌀 문자열
     * @param braceType : 괄호 종류 <code>(, [, {, <</code> 그 외 경우에는 기본값으로 <code>(</code>
     * @return <code>braceType_Open + str + braceType_Close</code>
     * @apiNote <code> wrap("Hello: World!", '{'); -> "{Hello: World!}"</code>
     */
    public static String wrap(String str, char braceType) {
        switch (braceType) {
            default:
            case '(': case ')': return "(" + str + ")";
            case '[': case ']': return "[" + str + "]";
            case '{': case '}': return "{" + str + "}";
            case '<': case '>': return "<" + str + ">";
        }
    }

    /**
     * <p>문자열을 <code>Snake_Case</code>로 변환하여 반환합니다.</p>
     * @param str : 원본 문자열.
     * @return SnakeCase로 변환된 문자열.
     * @apiNote <code>ThisIsSnakeCase -> this_is_snake_case</code>
     */
    public static String toSnakeCase(String str) {
        if (str == null) return null;

        final StringBuilder rtSb = new StringBuilder(str.length());
        final char[] strChAry = str.toCharArray();
        for (int i = 0; i < strChAry.length; ++i) {
            char ch = strChAry[i];
            if (ch > '@' && ch < '[') { // A(65) ~ Z(90) (UpperCase)
                if (i > 0) {
                    rtSb.append('_');
                }

                ch += 32; // A(65) -> a(97)
            }
            
            rtSb.append(ch);
        }

        return rtSb.toString();
    }
}