package com.bcsdlab.bcsdinternalapiv2.game.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class GameException extends BcsdException {

    private final BcsdExceptionType exceptionType;

    public GameException(BcsdExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public BcsdExceptionType getExceptionType() {
        return exceptionType;
    }
}
