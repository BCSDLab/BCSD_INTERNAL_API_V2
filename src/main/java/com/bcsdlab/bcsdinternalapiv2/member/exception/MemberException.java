package com.bcsdlab.bcsdinternalapiv2.member.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class MemberException extends BcsdException {

    public MemberException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
