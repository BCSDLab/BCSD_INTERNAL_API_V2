package com.bcsdlab.bcsdinternalapiv2.game;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** 시크릿이 설정되지 않은 서버는 빈 헤더를 포함한 모든 웹훅을 거부한다(이전에는 빈 시크릿과 빈 헤더가 일치했다). */
class GameBuildWebhookNoSecretIntegrationTest extends IntegrationTestSupport {

    @Test
    void 시크릿이_설정되지_않으면_헤더_없는_웹훅도_401이다() throws Exception {
        mockMvc.perform(post("/v1/games/builds/1/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 시크릿이_설정되지_않으면_빈_헤더_웹훅도_401이다() throws Exception {
        mockMvc.perform(post("/v1/games/builds/1/webhook")
                        .header("X-Game-Build-Secret", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());
    }
}
