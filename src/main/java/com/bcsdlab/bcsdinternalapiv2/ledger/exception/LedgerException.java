package com.bcsdlab.bcsdinternalapiv2.ledger.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class LedgerException extends BcsdException {

    public LedgerException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
