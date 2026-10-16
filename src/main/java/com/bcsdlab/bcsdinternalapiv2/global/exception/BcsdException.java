package com.bcsdlab.bcsdinternalapiv2.global.exception;

public abstract class BcsdException extends RuntimeException {

    protected BcsdException() {
    }

    public abstract BcsdExceptionType getExceptionType();

    /** 로그에 예외 메시지가 남도록 예외 타입의 메시지를 돌려준다. */
    @Override
    public String getMessage() {
        return getExceptionType().getMessage();
    }
}
