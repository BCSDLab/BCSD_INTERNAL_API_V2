package com.bcsdlab.bcsdinternalapiv2.global;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

/**
 * 인터널 프론트(별도 오리진, credentials: 'include')가 브라우저에서 API를 직접 호출하려면
 * CORS 응답 헤더가 필요하다(T-26). 허용 오리진은 app.cors.allowed-origins
 * (기본 http://localhost:3000)로 설정한다.
 */
class CorsIntegrationTest extends IntegrationTestSupport {

    private static final String ALLOWED_ORIGIN = "http://localhost:3000";

    @Test
    @DisplayName("허용된 오리진의 실제 요청에 Allow-Origin·Allow-Credentials 헤더가 붙는다")
    void 허용된_오리진_요청에_cors_헤더가_붙는다() throws Exception {
        mockMvc.perform(get("/v1/activity-categories").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("허용된 오리진의 preflight(OPTIONS)가 통과한다")
    void preflight_요청이_통과한다() throws Exception {
        mockMvc.perform(options("/v1/auth/login")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    @DisplayName("허용되지 않은 오리진에는 Allow-Origin 헤더가 없다")
    void 허용되지_않은_오리진에는_cors_헤더가_없다() throws Exception {
        mockMvc.perform(get("/v1/activity-categories").header(HttpHeaders.ORIGIN, "http://evil.example"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
