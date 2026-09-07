package com.bcsdlab.bcsdinternalapiv2.global.exception;

public class GlobalException extends BcsdException {

    private final BcsdExceptionType exceptionType;

    public GlobalException(BcsdExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BcsdExceptionType getExceptionType() {
        return exceptionType;
    }
}
