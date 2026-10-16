package com.bcsdlab.bcsdinternalapiv2.curriculum.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class CurriculumException extends BcsdException {

    public CurriculumException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
