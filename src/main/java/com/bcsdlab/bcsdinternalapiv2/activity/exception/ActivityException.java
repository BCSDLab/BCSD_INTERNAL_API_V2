package com.bcsdlab.bcsdinternalapiv2.activity.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class ActivityException extends BcsdException {

    public ActivityException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
