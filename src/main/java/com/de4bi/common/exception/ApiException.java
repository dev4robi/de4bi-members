package com.de4bi.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    protected HttpStatus httpStatus = HttpStatus.OK;

    public ApiException() {
        super();
    }

    public ApiException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ApiException(String msg) {
        super(msg);
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

    public ApiException(HttpStatus httpStatus, String msg, Throwable t) {
        super(msg, t);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}