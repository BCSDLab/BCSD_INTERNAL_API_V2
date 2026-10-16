package com.bcsdlab.bcsdinternalapiv2.global;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** 클라이언트가 잘못 보낸 요청은 500이 아니라 알맞은 4xx와 {message} 본문으로 응답한다. */
class ClientErrorStatusIntegrationTest extends IntegrationTestSupport {

    @Test
    void 지원하지_않는_HTTP_메서드는_405다() throws Exception {
        mockMvc.perform(delete("/health"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."));
    }

    @Test
    void 지원하지_않는_Content_Type은_415다() throws Exception {
        mockMvc.perform(post("/v1/games/builds/1/webhook")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("status=ACTIVE"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("지원하지 않는 Content-Type입니다."));
    }

    @Test
    void 숫자가_아닌_경로_id는_400이다() throws Exception {
        mockMvc.perform(post("/v1/games/builds/abc/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("'buildId'의 형식이 올바르지 않습니다."));
    }

    @Test
    void 필수_쿼리_파라미터가_없으면_400이다() throws Exception {
        mockMvc.perform(get("/v1/activities"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("필수 요청 파라미터 'category'가 없습니다."));
    }
}
