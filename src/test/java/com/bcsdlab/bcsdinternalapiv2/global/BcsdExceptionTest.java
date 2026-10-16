package com.bcsdlab.bcsdinternalapiv2.global;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import org.junit.jupiter.api.Test;

class BcsdExceptionTest {

    @Test
    void 도메인_예외의_메시지는_예외_타입의_메시지다() {
        GameException exception = new GameException(GameExceptionType.GAME_NOT_FOUND);

        assertThat(exception.getMessage()).isEqualTo(GameExceptionType.GAME_NOT_FOUND.getMessage());
    }
}
