package com.de4bi.common.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    
    /**
     * <p>Map을 JSON문자열로 변환합니다.</p>
     * @param objMap : 변환할 객체.
     * @return JSON문자열.
     */
    public static String toJsonStr(Map<String, Object> objMap) {
        if (objMap == null) {
            return "null";
        }

        String rtStr = null;
        try {
            final ObjectMapper objMapper = new ObjectMapper();
            rtStr = objMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objMap);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Fail to convert Map to JSON!", e);
        }

        return rtStr;
    }

    /**
     * <p>JSON문자열을 Map으로 변환합니다.</p>
     * @param jsonStr : 변환할 문자열.
     * @return Map객체.
     */
    public static Map<String, Object> fromJsonStr(String jsonStr) {
        if (jsonStr == null || jsonStr.equals("null")) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> rtMap = null;
        try {
            final ObjectMapper objMapper = new ObjectMapper();
            rtMap = objMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>(){});
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Fail to parse JSON to Map!", e);
        }

        return rtMap;
    }
}