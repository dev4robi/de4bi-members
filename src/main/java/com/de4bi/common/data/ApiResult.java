package com.de4bi.common.data;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonPropertyOrder({"tid", "result", "code", "message", "data"})
@JsonIgnoreProperties("msgParamList")
public final class ApiResult<T> {

    @JsonIgnore
    private final Map<String, Object> resultMap;

    @JsonIgnore
    private final List<String> msgParamList;

    public static final String KEY_TID      = "tid";        // 추적 아이디 (trace_id)
    public static final String KEY_RESULT   = "result";     // 결과 (API result)
    public static final String KEY_CODE     = "code";       // 응답 코드 (Result code)
    public static final String KEY_MESSAGE  = "message";    // 메시지 (Result message)
    public static final String KEY_DATA     = "data";       // 데이터 (Result data)

    /**
     * <p>ApiResult객체를 생성합니다.</p>
     * @param resultMap : 객체 내부에서 데이터를 관리하기 위해 사용할 Map입니다. null전달 시 HashMap을 사용합니다.
     */
    private ApiResult(Map<String, Object> resultMap) {
        this.resultMap = (resultMap == null ? new LinkedHashMap<>() : resultMap);
        this.msgParamList = new LinkedList<>();
        this.resultMap.put(KEY_TID, ThreadStorage.get(KEY_TID));
        this.resultMap.put(KEY_RESULT, true);
        this.resultMap.put(KEY_CODE, null);
        this.resultMap.put(KEY_MESSAGE, null);
        this.resultMap.put(KEY_DATA, null);
    }

    /**
     * ApiResult객체를 생성합니다.
     * @param result : 결과
     * @param code : 응답코드
     * @param message : 메시지
     * @param data : 응답데이터
     */
    public static <T> ApiResult<T> of(boolean result, String code, String message, T data) {
        // [Note] 오직 생성자와 이 메서드에서만 'KEY_RESULT/KEY_CODE/KEY_MESSAGE/KEY_DATA'
        // 값에 대한 직접 추가(put)를 허용합니다. 다른 메서드에서는 외부로 노출된 Setter들을 사용하세요.
        final ApiResult<T> rtResult = new ApiResult<T>(new LinkedHashMap<>());
        rtResult.resultMap.put(KEY_RESULT, result);
        rtResult.resultMap.put(KEY_CODE, code);
        rtResult.resultMap.put(KEY_MESSAGE, message);
        rtResult.resultMap.put(KEY_DATA, data);
        return rtResult;
    }

    /**
     * @param result : 결과
     * @param clazz : 데이터부 클래스형
     * @return 생성된 {@link ApiResult}를 반환합니다.
     */
    public static <T> ApiResult<T> of(boolean result, Class<T> clazz) {
        return of(result, null, null, null);
    }

    /**
     * @param apiResult : 래핑(wrapping)할 원본 객체
     * @param clazz : 새로 생성하는 객체의 데이터부 클래스형
     * @return 생성된 {@link ApiResult}를 반환합니다.
     * @apiNote 생성된 결과를 래핑할 경우 사용합니다.
     * <code>result,code,message</code>는 <code>apiResult</code>로부터 복사하지만
     * <code>data부는 null</code>로 설정함에 유의해야 합니다.
     */
    public static <T> ApiResult<T> of(ApiResult<?> apiResult, Class<T> clazz) {
        return of(apiResult.getResult(), apiResult.getCode(), apiResult.getMessage(), null);
    }

    /**
     * @param result : 결과
     * @return 생성된 {@link ApiResult}를 반환합니다.
     */
    public static ApiResult<Void> of(boolean result) {
        return of(result, null, null, null);
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
     * @return String 'code'를 반환합니다.
     */
    public String getCode() {
        final Object rtObj = this.resultMap.get(KEY_CODE);
        return rtObj == null ? null : rtObj.toString();
    }

    /**
     * @param code : 설정할 'code'값
     * @return ApiResult자신을 반환합니다.
     */
    public ApiResult<T> setCode(String code) {
        this.resultMap.put(KEY_CODE, code);
        return this;
    }

    /**
     * @return String 'message'를 반환합니다.
     */
    public String getMessage() {
        final Object rtObj = this.resultMap.get(KEY_MESSAGE);
        return rtObj == null ? null : rtObj.toString();
    }

    /**
     * @param code : 설정할 'message'값
     * @return ApiResult자신을 반환합니다.
     */
    public ApiResult<T> setMessage(String message) {
        this.resultMap.put(KEY_MESSAGE, message);
        return this;
    }

    /**
     * @return 메시지 파라미터 <code>List&lt;String></code>
     */
    public List<String> getMsgParamList() {
        return this.msgParamList;
    }

    /**
     * @param idx : 추가할 매개변수 인덱스 (0~)
     * @param param : 추가할 메시지 매개변수
     * @return ApiResult자신을 반환합니다.
     * @apiNote 추가된 총 매개변수보다 <code>idx</code>값이 더 크면 <code>idx</code>만큼 빈 문자열("")을 리스트에 채워넣습니다.
     */
    public ApiResult<T> setMsgParam(int idx, String param) {
        while (this.msgParamList.size() > idx) {
            this.msgParamList.add("");
        }
        this.msgParamList.set(idx, param);
        return this;
    }

    /**
     * @param param : 추가할 메시지 매개변수
     * @return ApiResult자신을 반환합니다.
     */
    public ApiResult<T> addMsgParam(String param) {
        this.msgParamList.add(param);
        return this;
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
     * <p>ApiResult에서 'data'파트를 갱신합니다.</p>
     * @param data : 'data'파트에 저장할 객체
     * @return ApiResult자신을 반환합니다.
     */
    public ApiResult<T> setData(T data) {
        this.resultMap.put(KEY_DATA, data);
        return this;
    }

    /**
     * <p>ApiResult를 JSON문자열로 변환하여 반환합니다.</p>
     * 변환중 오류가 발생하거나 변환할 데이터가 없다면 "{}"를 반환합니다.
     * @return JSON포멧 문자열
     * @apiNote <code>List&lt;String>msgParamList</code>은 toString()에서 출력하지 않습니다.
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