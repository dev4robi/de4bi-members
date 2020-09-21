package com.de4bi.common.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import lombok.Data;

@Data
public class ApiResult<T> {

    private final Map<String, Object> resultMap;

    public static final String KEY_TID      = "tid";
    public static final String KEY_RESULT   = "result";
    public static final String KEY_MESSAGE  = "message";
    public static final String KEY_DATA     = "data";

    /**
     * <p>ApiResult객체를 생성합니다.</p>
     * @param resultMap - 객체 내부에서 데이터를 관리하기 위해 사용할 Map입니다. null전달 시 HashMap을 사용합니다.
     */
    private ApiResult(Map<String, Object> resultMap) {
        this.resultMap = (resultMap == null ? new HashMap<>() : resultMap);
        this.resultMap.put(KEY_TID, ThreadStorage.get(KEY_TID));
        this.resultMap.put(KEY_RESULT, true);
        this.resultMap.put(KEY_MESSAGE, null);
        this.resultMap.put(KEY_DATA, null);
    }

    /**
     * ApiResult객체를 생성합니다.
     * @param result - 결과
     * @param message - 메시지
     * @param data - 응답데이터
     */
    public static <T> ApiResult<T> of(boolean result, String message, T data) {
        final ApiResult<T> rtResult = new ApiResult<T>(new LinkedHashMap<>());
        // [Note] 오직 생성자와 이 메서드에서만 'KEY_RESULT/KEY_MESSAGE/KEY_DATA'
        // 값에 대한 직접 추가(put)를 허용합니다. 다른 메서드에서는 외부로 노출된 Setter들을 사용하세요.
        rtResult.resultMap.put(KEY_RESULT, result);
        rtResult.resultMap.put(KEY_MESSAGE, message);
        rtResult.resultMap.put(KEY_DATA, data);
        return rtResult;
    }

    /**
     * ApiResult객체를 생성합니다.
     * @param result - 결과
     * @param message - 메시지
     */
    public static <T> ApiResult<T> of(boolean result, String message) {
        return of(result, message, null);
    }

    /**
     * ApiResult객체를 생성합니다.
     * @param result - 결과
     * @param data - 응답데이터
     */
    public static <T> ApiResult<T> of(boolean result, T data) {
        return of(result, null, data);
    }

    /**
     * ApiResult객체를 생성합니다.
     * @param result - 결과
     */
    public static <T> ApiResult<T> of(boolean result) {
        return of(result, null, null);
    }

    /**
     * @return String 'tid'를 반환합니다.
     */
    public String getTid() {
        final Object rtObj = this.resultMap.get(KEY_TID);
        return rtObj == null ? null : rtObj.toString();
    }

    /**
     * @return boolean 'result'를 반환합니다.
     */
    public boolean getResult() {
        final Boolean rtBool = (Boolean)this.resultMap.get(KEY_RESULT);
        return rtBool.booleanValue();
    }

    /**
     * @return String 'message'를 반환합니다.
     */
    public String getMessage() {
        final Object rtObj = this.resultMap.get(KEY_MESSAGE);
        return rtObj == null ? null : rtObj.toString();
    }

    /**
     * @return <T>타입 'data'를 반환합니다.
     */
    public T getData() {
        @SuppressWarnings("unchecked")
        // [Note] ApiResult<T> of(boolean result, String message, T data) 메서드를 통해서 생성된
        // 정상적인 클래스라면 이곳에서 항상 (T)타입 클래스를 반환합니다.
        final T rtObj = (T)this.resultMap.get(KEY_DATA);
        return rtObj;
    }

    /**
     * <p>ApiResult에서 'data'파트를 저장합니다.
     * @param data - 'data'파트에 저장할 객체.
     * @return 기존 'data'파트에 저장돼 있었던 객체.
     */
    public T setData(T data) {
        @SuppressWarnings("unchecked")
        // [Note] ApiResult<T> of(boolean result, String message, T data) 메서드를 통해서 생성된
        // 정상적인 클래스라면 이곳에서 항상 (T)타입 클래스를 반환합니다.
        final T rtObj = (T)this.resultMap.put(KEY_DATA, data);
        return rtObj;
    }

    /**
     * <p>ApiResult를 JSON문자열로 변환하여 반환합니다.</p>
     * 변환중 오류가 발생하거나 변환할 데이터가 없다면 "{}"를 반환합니다.
     * @return JSON포멧 문자열.
     */
    @Override
    public String toString() {
        final ObjectMapper objMapper = new ObjectMapper();
        String rtStr = null;
        try {
            objMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            rtStr = objMapper.writeValueAsString(this.resultMap);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException("Fail to convert 'this.resultMap' to 'rtStr'!", e.getCause());
        }
        return Objects.nonNull(rtStr) ? rtStr : "{}";
    }
}