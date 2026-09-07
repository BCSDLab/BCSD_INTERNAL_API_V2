package com.bcsdlab.bcsdinternalapiv2.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link IntegrationTestSupport}(싱글턴 Postgres 컨테이너 베이스)가 실제로 동작하는지
 * 검증하는 최소 스모크 테스트도 겸한다(T-01).
 */
class HealthCheckIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("GET /health는 인증 없이 200과 OK를 반환한다")
    void health() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
