package com.bcsdlab.bcsdinternalapiv2.reservation.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class ReservationException extends BcsdException {

    public ReservationException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
