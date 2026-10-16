package com.bcsdlab.bcsdinternalapiv2.track.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class TrackException extends BcsdException {

    public TrackException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
