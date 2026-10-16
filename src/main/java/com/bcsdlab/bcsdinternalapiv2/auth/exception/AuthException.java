package com.bcsdlab.bcsdinternalapiv2.auth.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class AuthException extends BcsdException {

    public AuthException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
