package com.de4bi.common.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiResult {

    private final Map<String, Object> resultMap;

    public static final String KEY_TID = "tid";
    public static final String KEY_RESULT = "result";
    public static final String KEY_MESSAGE = "message";

    private ApiResult(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
        final Object tid = ThreadStorage.get(KEY_TID);
        if (Objects.nonNull(tid)) {
            this.resultMap.put(KEY_TID, tid);
        }
        this.resultMap.put(KEY_RESULT, true);
        this.resultMap.put(KEY_MESSAGE, null);
    }

    public static ApiResult of(boolean result, String message) {
        final ApiResult rtResult = new ApiResult(new LinkedHashMap<>());
        rtResult.put(KEY_RESULT, result);
        if (Objects.nonNull(message)) {
            rtResult.put(KEY_MESSAGE, message);
        }
        return rtResult;
    }

    public static ApiResult of(boolean result, String message, Map<String, Object> dataMap) {
        final ApiResult rtResult = of(result, message);
        rtResult.putAll(dataMap);
        return rtResult;
    }

    public static ApiResult of() {
        return new ApiResult(null);
    }

    public Object get(String key) {
        return this.resultMap.get(key);
    }

    public String getStr(String key) {
        final Object tempObj = this.resultMap.get(key);
        return Objects.nonNull(tempObj) ? tempObj.toString() : null;
    }

    public Object put(String key, Object value) {
        return this.resultMap.put(key, value);
    }

    public void putAll(Map<String, Object> dataMap) {
        this.resultMap.putAll(dataMap);
    }

    public void clear() {
        this.resultMap.clear();
    }

    public Set<String> keySet() {
        return this.resultMap.keySet();
    }

    @Override
    public String toString() {
        final ObjectMapper objMapper = new ObjectMapper();
        String rtStr = null;
        try {
            rtStr = objMapper.writeValueAsString(this.resultMap);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Fail to convert 'this.resultMap' to 'rtStr'!", e.getCause());
        }
        return Objects.nonNull(rtStr) ? rtStr : "{}";
    }
}