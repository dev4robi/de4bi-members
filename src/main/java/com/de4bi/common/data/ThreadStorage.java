package com.de4bi.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 스레드 스토레지입니다. 내부적으로 {@code static ThreadLocal}을 사용하여 구성되어 있습니다.
 * thread-safe하며, 같은 key를 가진 데이터를 {@code put()}하여도, 스레드별로 구분되어 저장됩니다.
 * 스레드가 소멸하면 해당 데이터 또한 해제됩니다.
 */
public class ThreadStorage {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 스레드 스토레지의 데이터 저장용 Map자체를 반환합니다.
     * 기존에 생성된 Map이 없는 경우, 새로운 Map을 생성하여 반환합니다.
     * 
     * @return 스레드 스토레지 내부에서 사용하는 데이터 저장용 Map.
     */
    public static Map<String, Object> getThreadLocalMap() {
        Map<String, Object> rtMap = THREAD_LOCAL.get();
        if (Objects.isNull(rtMap)) {
            rtMap = new HashMap<>();
            THREAD_LOCAL.set(rtMap);
        }
        return rtMap;
    }

    /**
     * 스레드 스토레지에서 데이터를 가져옵니다.
     * 
     * @param key - 데이터 키값입니다.
     * @return 저장된 데이터가 있으면 해당 객체를, 없으면 null을 반환합니다.
     */
    public static Object get(String key) {
        return getThreadLocalMap().get(key);
    }

    /**
     * 스레드 스토레지에서 데이터를 문자열로 가져옵니다.
     * 
     * @param key - 데이터 키값입니다.
     * @return 저장된 데이터가 있으면 해당 객체의 {@code toString()}값을, 없으면 null을 반환합니다.
     */
    public static String getStr(String key) {
        final Object tempObj = getThreadLocalMap().get(key);
        return Objects.nonNull(tempObj) ? tempObj.toString() : null;
    }

    /**
     * 스레드 스토레지에 데이터를 저장합니다.
     * 
     * @param key - 데이터 키값입니다.
     * @param value - 데이터 값입니다.
     * @return 같은 키로 저장된 데이터가 있었다면 해당 데이터를, 그 외에는 null을 반환합니다.
     */
    public static Object put(String key, Object value) {
        return getThreadLocalMap().put(key, value);
    }

    /**
     * 스레드 스토레지에 데이터를 저장합니다.
     * 
     * @param dataMap - 데이터 맵입니다.
     */
    public static void putAll(Map<String, Object> dataMap) {
        getThreadLocalMap().putAll(dataMap);
    }

    /**
     * 스레드 스토레지에 데이터 저장용 Map을 초기화합니다.
     */
    public static void clear() {
        getThreadLocalMap().clear();
    }
}