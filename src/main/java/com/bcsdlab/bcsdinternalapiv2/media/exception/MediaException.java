package com.bcsdlab.bcsdinternalapiv2.media.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class MediaException extends BcsdException {

    public MediaException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
