package com.bcsdlab.bcsdinternalapiv2.home.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class HomeException extends BcsdException {

    private final BcsdExceptionType exceptionType;

    public HomeException(BcsdExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BcsdExceptionType getExceptionType() {
        return exceptionType;
    }
}
