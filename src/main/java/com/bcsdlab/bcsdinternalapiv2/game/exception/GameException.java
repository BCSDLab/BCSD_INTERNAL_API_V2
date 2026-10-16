package com.bcsdlab.bcsdinternalapiv2.game.exception;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;

public class GameException extends BcsdException {

    public GameException(BcsdExceptionType exceptionType) {
        super(exceptionType);
    }
}
