package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** 서버에 SLACK_BOT_TOKEN이 없을 때: Slack을 부르지 않고 회원별 SLACK_ERROR로 기록한다. 설정이 달라 컨텍스트가 하나 더 뜬다. */
@TestPropertySource(properties = "app.slack.bot-token=")
class DuesNotificationNoTokenIntegrationTest extends LedgerIntegrationTestSupport {

    @Test
    void 봇_토큰이_없으면_Slack을_부르지_않고_회원별_SLACK_ERROR로_기록한다() throws Exception {
        Member alice = member("20240001", "가회원").save();
        jdbcTemplate.update("UPDATE member SET slack_id = 'U0ALICE001' WHERE id = ?", alice.getId());
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        perform(post("/v1/admin/dues/semesters/2026-2/notifications").header("Idempotency-Key", "no-token-key-0001"),
                adminToken, "{\"type\":\"PAYMENT\",\"messages\":[{\"memberId\":%d,\"message\":\"안내\"}]}"
                        .formatted(alice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(0))
                .andExpect(jsonPath("$.failed[0].memberId").value(alice.getId().intValue()))
                .andExpect(jsonPath("$.failed[0].reason").value("SLACK_ERROR"));

        verifyNoInteractions(slackClient);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status || ':' || failure_reason FROM dues_notification_recipient", String.class))
                .isEqualTo("FAILED:SLACK_ERROR");
    }
}
