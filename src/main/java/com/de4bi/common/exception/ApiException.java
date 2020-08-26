package com.de4bi.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    protected HttpStatus httpStatus = HttpStatus.OK; // HTTP Status
    protected String internalMsg = null; // 내부 로깅용 메시지

    public ApiException() {
        super();
    }

    public ApiException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ApiException(String msg) {
        super(msg);
    }

    public ApiException(String msg, String internalMsg) {
        super(msg);
        this.internalMsg = internalMsg;
    }

    public ApiException(Throwable t) {
        super(t);
    }

    public ApiException(HttpStatus httpStatus, String msg) {
        super(msg);
        this.httpStatus = httpStatus;
    }

    public ApiException(String msg, Throwable t) {
        super(msg, t);
    }

    public ApiException(String msg, String internalMsg, Throwable t) {
        super(msg, t);
        this.internalMsg = internalMsg;
    }

    public ApiException(HttpStatus httpStatus, String msg, Throwable t) {
        super(msg, t);
        this.httpStatus = httpStatus;
    }

    public ApiException(HttpStatus httpStatus, String msg, String internalMsg, Throwable t) {
        super(msg, t);
        this.httpStatus = httpStatus;
        this.internalMsg = internalMsg;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    public ApiException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
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