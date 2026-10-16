package com.bcsdlab.bcsdinternalapiv2.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

/** 토큰에서 채우는 파라미터(@LoginMemberId, Jwt)는 API 문서에 요청 파라미터로 나오지 않는다. */
class ApiDocsIntegrationTest extends IntegrationTestSupport {

    @Test
    void 로그인_회원_파라미터는_문서에_노출되지_않는다() throws Exception {
        String docs = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(docs)
                .contains("/v1/reservations/daily")
                .doesNotContain("\"loginMemberId\"")
                .doesNotContain("\"viewerMemberId\"")
                .doesNotContain("\"jwt\"");
    }
}
