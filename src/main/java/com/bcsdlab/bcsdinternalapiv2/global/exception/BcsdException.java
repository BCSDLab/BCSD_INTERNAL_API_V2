package com.bcsdlab.bcsdinternalapiv2.global.exception;

public abstract class BcsdException extends RuntimeException {

    protected BcsdException() {
    }

    public abstract BcsdExceptionType getExceptionType();
}
