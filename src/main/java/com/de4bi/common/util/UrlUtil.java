package com.de4bi.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UrlUtil {

    /**
     * @param str : 변환할 문자열.
     * @param charset : 인코딩 체어셋.
     * @return URL Encoding한 문자열을 반환합니다.
     */
    public static String UrlEncoding(String str, String charset) {
        String rtStr = null;
        if (str != null) {
            try {
                rtStr = URLEncoder.encode(str, charset);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Unsupported encoding! (charset: " + charset + ")", e);
            }
        }
        return rtStr;
    }

    /**
     * @param str : 변환할 문자열.
     * @param charset : 디코딩 체어셋.
     * @return URL Decoding한 문자열을 반환합니다.
     */
    public static String UrlDecoding(String str, String charset) {
        String rtStr = null;
        if (str != null) {
            try {
                rtStr = URLDecoder.decode(str, charset);
            }
            catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("Unsupported encoding! (charset: " + charset + ")", e);
            }
        }
        return rtStr;
    }
    
    /**
     * <p>전달된 Map내부의 값들을 URL에 사용할 수 있는 문자열로 생성합니다.</p>
     * <p>paramMap에 key1, key2, key3가 각각 val1, val2, val3의 값으로 들어 있는 경우.</p>
     * <p>{@code key1%3Dval1%60key2%3Dval2%60key3%3Dval3 (delimiter: "`")}</p>
     * <p>{@code key1%7Cval1%60key2%7Cval2%60key3%7Cval3 (delimiter: "|")}</p>
     * @param paramMap : 변환할 데이터를 담은 맵. (nullable)
     * @param delimiter : 데이터 사이의 구분자 문자열. <strong>(데이터와 겹치지 않도록 주의!)</strong>
     * @return 생성된 {@code keyN=valN}형식의 데이터를 UTF-8 URL Encoding하여 반환합니다.
     */
    public static String makeUrlParam(Map<String, Object> paramMap, String delimiter) {
        Objects.requireNonNull(delimiter, "'delimiter' is null!");
        final StringBuilder rtSb = new StringBuilder(128);
        if (paramMap != null) {
            for (String key : paramMap.keySet()) {
                rtSb.append(key).append('=').append(paramMap.get(key)).append(delimiter);
            }
        }
        rtSb.setLength(rtSb.length() > 0 ? rtSb.length() - 1 : rtSb.length()); // 맨 마지막 {delimiter}제거
        return UrlUtil.UrlEncoding(rtSb.toString(), "UTF-8");
    }

    /**
     * <p>전달된 문자열 파라미터(urlParam)을 파싱하여 Map으로 반환합니다.</p>
     * <p>urlParamStr이 아래와 같은 경우,</p>
     * <p>{@code key1%3Dval1%60key2%3Dval2%60key3%3Dval3 (delimiter: "`")}</p>
     * <p>Map에는 {@code key1=val1, key2=key2, key3=val3}값이 들어있게 됩니다.</p>
     * @param urlParamStr : 파싱할 문자열. (nullable)
     * @param delimiter : 데이터 사이의 구분자 문자열.
     * @return 파싱된 결과를 담은 Map을 반환합니다.
     */
    public static Map<String, Object> parseUrlParam(String urlParamStr, String delimiter) {
        Objects.requireNonNull(delimiter, "'delimiter' is null!");
        final Map<String, Object> rtMap = new HashMap<>();
        if (urlParamStr != null) {
            urlParamStr = UrlDecoding(urlParamStr, "UTF-8");
            for (String keyVal : urlParamStr.split(delimiter)) { // key=val
                final String[] keyAndValue = keyVal.split("="); // [0]:key, [1]:value
                if (keyAndValue.length == 2) {
                    rtMap.put(keyAndValue[0], keyAndValue[1]);
                }
            }
        }
        return rtMap;
    }
}
