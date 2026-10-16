package com.bcsdlab.bcsdinternalapiv2.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bcsdlab.bcsdinternalapiv2.game.config.GameBuildProperties;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.service.GameBuildTokenService;
import org.junit.jupiter.api.Test;

class GameBuildPropertiesTest {

    @Test
    void 설정된_시크릿과_같은_값만_일치한다() {
        GameBuildProperties properties = withSecret("shared-secret");

        assertThat(properties.matchesSecret("shared-secret")).isTrue();
        assertThat(properties.matchesSecret("shared-secreT")).isFalse();
        assertThat(properties.matchesSecret("")).isFalse();
        assertThat(properties.matchesSecret(null)).isFalse();
    }

    @Test
    void 시크릿이_비어_있으면_어떤_값도_일치하지_않는다() {
        assertThat(withSecret("").matchesSecret("")).isFalse();
        assertThat(withSecret(null).matchesSecret("")).isFalse();
        assertThat(withSecret("   ").matchesSecret("   ")).isFalse();
    }

    @Test
    void 시크릿이_비어_있으면_업로드_토큰을_발급하지_않는다() {
        GameBuildTokenService tokenService = new GameBuildTokenService(withSecret(""));

        assertThatThrownBy(() -> tokenService.issue(1L, 1L, "slug", "1.0.0"))
                .isInstanceOf(GameException.class);
    }

    private static GameBuildProperties withSecret(String secret) {
        return new GameBuildProperties("http://localhost/upload", secret, 1800, "https://bcsdlab.com");
    }
}
