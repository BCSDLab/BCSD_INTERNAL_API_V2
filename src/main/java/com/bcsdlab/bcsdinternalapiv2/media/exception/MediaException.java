package com.bcsdlab.bcsdinternalapiv2.media.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class MediaException extends BcsdException {

    private final BcsdExceptionType exceptionType;

    public MediaException(BcsdExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BcsdExceptionType getExceptionType() {
        return exceptionType;
    }
}
