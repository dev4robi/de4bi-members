package com.de4bi.common.exception;

public class MapperException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public MapperException() {
        super();
    }

    public MapperException(String msg) {
        super(msg);
    }

    public MapperException(Throwable t) {
        super(t);
    }

    public MapperException(String msg, Throwable t) {
        super(msg, t);
    }
}