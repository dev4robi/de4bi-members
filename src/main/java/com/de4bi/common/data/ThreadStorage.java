package com.de4bi.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThreadStorage {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static Map<String, Object> getThreadLocalMap() {
        Map<String, Object> rtMap = THREAD_LOCAL.get();
        if (Objects.isNull(rtMap)) {
            rtMap = new HashMap<>();
            THREAD_LOCAL.set(rtMap);
        }
        return rtMap;
    }

    public static Object get(String key) {
        return getThreadLocalMap().get(key);
    }

    public static String getStr(String key) {
        final Object tempObj = getThreadLocalMap().get(key);
        return Objects.nonNull(tempObj) ? tempObj.toString() : null;
    }

    public static Object put(String key, Object value) {
        return getThreadLocalMap().put(key, value);
    }

    public static void putAll(Map<String, Object> dataMap) {
        getThreadLocalMap().putAll(dataMap);
    }

    public static void clear() {
        getThreadLocalMap().clear();
    }
}