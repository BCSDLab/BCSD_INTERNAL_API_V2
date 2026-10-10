package com.bcsdlab.bcsdinternalapiv2.ledger.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class LedgerException extends BcsdException {

    private final BcsdExceptionType exceptionType;

    public LedgerException(BcsdExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BcsdExceptionType getExceptionType() {
        return exceptionType;
    }
}
