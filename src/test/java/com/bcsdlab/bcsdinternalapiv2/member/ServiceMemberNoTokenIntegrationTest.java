package com.bcsdlab.bcsdinternalapiv2.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** 서버에 BBOT_SERVICE_TOKEN이 없을 때: 어떤 요청도 통과시키지 않는다(fail closed). 설정이 달라 컨텍스트가 하나 더 뜬다. */
@TestPropertySource(properties = "app.service.bbot-token=")
class ServiceMemberNoTokenIntegrationTest extends IntegrationTestSupport {

    @Test
    void 서비스_토큰이_설정되지_않으면_빈_Bearer도_401이다() throws Exception {
        mockMvc.perform(get("/v1/service/members").header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/service/members").header("Authorization", "Bearer  "))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/service/members"))
                .andExpect(status().isUnauthorized());
    }
}
