package com.de4bi.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    protected HttpStatus httpStatus = HttpStatus.OK;    // HTTP Status
    protected String externalMsg = null;                // 외부 출력용 메시지
    protected String internalMsg = null;                // 내부 로깅용 메시지

    // 생성자
    private ApiException(HttpStatus httpStatus, String externalMsg, String internalMsg, Throwable t) {
        super(t == null ? null : t.getMessage(), t);
        this.httpStatus = httpStatus;
        this.externalMsg = externalMsg;
        this.internalMsg = internalMsg;
    }

    // 정적 생성자
    public static ApiException of(HttpStatus httpStatus, String externalMsg, String internalMsg, Throwable t) {
        final ApiException rtExc = new ApiException(httpStatus, externalMsg, internalMsg, t);
        return rtExc;
    }

    public static ApiException of(HttpStatus httpStatus, String externalMsg, String internalMsg) {
        final ApiException rtExc = new ApiException(httpStatus, externalMsg, internalMsg, null);
        return rtExc;
    }

    public static ApiException of(String externalMsg, String internalMsg, Throwable t) {
        final ApiException rtExc = new ApiException(HttpStatus.OK, externalMsg, internalMsg, t);
        return rtExc;
    }

    public static ApiException of(String externalMsg, String internalMsg) {
        final ApiException rtExc = new ApiException(HttpStatus.OK, externalMsg, internalMsg, null);
        return rtExc;
    }

    public static ApiException of(String externalMsg) {
        final ApiException rtExc = new ApiException(HttpStatus.OK, externalMsg, null, null);
        return rtExc;
    }

    public static ApiException of() {
        final ApiException rtExc = new ApiException(HttpStatus.OK, null, null, null);
        return rtExc;
    }

    // 게터 & 세터
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    public ApiException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public String getExternalMsg() {
        return this.internalMsg;
    }

    public ApiException setExternalMsg(String msg) {
        this.externalMsg = msg;
        return this;
    }

    public String getInternalMsg() {
        return this.internalMsg;
    }

    public ApiException setInternalMsg(String msg) {
        this.internalMsg = msg;
        return this;
    }
}