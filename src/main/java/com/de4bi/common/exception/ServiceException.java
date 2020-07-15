package com.de4bi.common.exception;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ServiceException() {
        super();
    }

    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(Throwable t) {
        super(t);
    }

    public ServiceException(String msg, Throwable t) {
        super(msg, t);
    }
}