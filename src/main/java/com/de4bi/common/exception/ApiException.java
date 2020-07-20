package com.de4bi.common.exception;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    protected int httpStatus = 200;

    public ApiException() {
        super();
    }

    public ApiException(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ApiException(String msg) {
        super(msg);
    }

    public ApiException(Throwable t) {
        super(t);
    }

    public ApiException(int httpStatus, String msg, Throwable t) {
        super(msg, t);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }
}