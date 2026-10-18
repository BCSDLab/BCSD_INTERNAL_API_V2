package com.bcsdlab.bcsdinternalapiv2.home.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class HomeException extends BcsdException {

    public HomeException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
