package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackPostResult;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

/** Slack은 @MockitoBean SlackClient로 막는다. 실제 메시지는 보내지 않는다. */
class DuesNotificationIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String URL = "/v1/admin/dues/semesters/{semesterId}/notifications";
    private static final String ADMIN_SLACK = "U0ADMIN001";

    private Long adminId;
    private Member alice;
    private Member bob;
    private Member carol;
    private Member outsider;

    @BeforeEach
    void setUpMembers() throws Exception {
        adminId = memberRepository.findByStudentNumber("2020000000").orElseThrow().getId();
        jdbcTemplate.update("UPDATE member SET slack_id = ? WHERE id = ?", ADMIN_SLACK, adminId);
        alice = member("20240001", "가회원").save();
        bob = member("20240002", "나회원").save();
        carol = member("20240003", "다회원").save();
        setSlackId(alice, "U0ALICE001");
        setSlackId(bob, "U0BOB00001");
        setSlackId(carol, "U0CAROL001");
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        outsider = member("20240004", "라명단밖").save();
        when(slackClient.postDirectMessage(anyString(), anyString()))
                .thenAnswer(invocation -> new SlackPostResult("D" + invocation.getArgument(0, String.class), "1.0"));
    }

    // ---------- 발송 ----------

    @Test
    void 회원마다_DM을_보내고_보낸_즉시_기록한다() throws Exception {
        send(key(), body("PAYMENT", Map.of(alice.getId(), "안내 {담당자 멘션}", bob.getId(), "안내2"), false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(2))
                .andExpect(jsonPath("$.failed", hasSize(0)));

        verify(slackClient).postDirectMessage("U0ALICE001", "안내 <@U0ADMIN001>");
        verify(slackClient).postDirectMessage("U0BOB00001", "안내2");
        assertThat(jdbcTemplate.queryForList(
                "SELECT status || ':' || slack_channel || ':' || slack_ts FROM dues_notification_recipient ORDER BY member_id",
                String.class)).containsExactly("SENT:DU0ALICE001:1.0", "SENT:DU0BOB00001:1.0");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status || ':' || type || ':' || test_to_self || ':' || recipient_count FROM dues_notification_batch",
                String.class)).isEqualTo("DONE:PAYMENT:false:2");
    }

    @Test
    void 본문의_링크는_살리고_주입은_글자로_바꿔_보낸다() throws Exception {
        send(key(), body("UNPAID", Map.of(alice.getId(),
                "<https://a.com/list|납부 문서> & <!channel> <@U99999999> 문의: {담당자 멘션}"), false))
                .andExpect(status().isOk());

        verify(slackClient).postDirectMessage("U0ALICE001",
                "<https://a.com/list|납부 문서> &amp; &lt;!channel&gt; &lt;@U99999999&gt; 문의: <@U0ADMIN001>");
    }

    @Test
    void Slack_ID가_없는_회원과_Slack_오류는_회원별_실패이고_200이다() throws Exception {
        jdbcTemplate.update("UPDATE member SET slack_id = NULL WHERE id = ?", bob.getId());
        when(slackClient.postDirectMessage(eq("U0CAROL001"), anyString()))
                .thenThrow(new SlackApiException("channel_not_found", null));

        send(key(), body("PAYMENT", ordered(alice.getId(), bob.getId(), carol.getId()), false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(1))
                .andExpect(jsonPath("$.failed[*].memberId").value(contains(bob.getId().intValue(), carol.getId().intValue())))
                .andExpect(jsonPath("$.failed[*].reason").value(contains("SLACK_ID_MISSING", "SLACK_ERROR")));
    }

    @Test
    void 권한_오류와_네트워크_오류도_SLACK_ERROR다() throws Exception {
        when(slackClient.postDirectMessage(eq("U0ALICE001"), anyString()))
                .thenThrow(new SlackApiException("missing_scope", null));
        when(slackClient.postDirectMessage(eq("U0BOB00001"), anyString()))
                .thenThrow(new IllegalStateException("connection reset"));

        send(key(), body("PAYMENT", ordered(alice.getId(), bob.getId()), false))
                .andExpect(jsonPath("$.sentCount").value(0))
                .andExpect(jsonPath("$.failed[*].reason").value(contains("SLACK_ERROR", "SLACK_ERROR")));
    }

    // ---------- 관리자 Slack ID ----------

    @Test
    void 관리자_Slack_ID가_없는데_담당자_멘션이_있으면_보내기_전에_400이다() throws Exception {
        jdbcTemplate.update("UPDATE member SET slack_id = NULL WHERE id = ?", adminId);

        send(key(), body("UNPAID", Map.of(alice.getId(), "문의 {담당자 멘션}"), false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "내 Slack ID가 인명부에 없어 담당자 멘션을 넣을 수 없습니다. 인명부에서 Slack ID를 먼저 저장하세요."));
        verifyNoInteractions(slackClient);
        assertThat(batchCount()).isZero();

        send(key(), body("PAYMENT", Map.of(alice.getId(), "멘션 없는 안내"), false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(1));
    }

    // ---------- 검증 ----------

    @Test
    void 요청_검증_실패는_400이고_보내지_않는다() throws Exception {
        send(key(), "{\"type\":\"PAYMENT\",\"messages\":[]}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("보낼 회원을 선택하세요."));
        send(key(), "{\"messages\":[{\"memberId\":%d,\"message\":\"a\"}]}".formatted(alice.getId()))
                .andExpect(status().isBadRequest());
        send(key(), body("PAYMENT", Map.of(alice.getId(), "가".repeat(4001)), false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("메시지는 4,000자 이하여야 합니다."));
        send(key(), body("PAYMENT", Map.of(alice.getId(), " "), false)).andExpect(status().isBadRequest());

        ObjectNode many = objectMapper.createObjectNode().put("type", "PAYMENT");
        ArrayNode messages = many.putArray("messages");
        for (int i = 0; i < 51; i++) {
            messages.addObject().put("memberId", 1_000_000 + i).put("message", "a");
        }
        send(key(), many.toString()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("한 번에 50명까지 보낼 수 있습니다."));

        ObjectNode duplicated = objectMapper.createObjectNode().put("type", "PAYMENT");
        duplicated.putArray("messages")
                .add(objectMapper.createObjectNode().put("memberId", alice.getId()).put("message", "a"))
                .add(objectMapper.createObjectNode().put("memberId", alice.getId()).put("message", "b"));
        send(key(), duplicated.toString()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("같은 회원이 요청에 두 번 있습니다."));

        verifyNoInteractions(slackClient);
        assertThat(batchCount()).isZero();
    }

    @Test
    void 본문_4000자는_보낼_수_있다() throws Exception {
        send(key(), body("PAYMENT", Map.of(alice.getId(), "가".repeat(4000)), false)).andExpect(status().isOk());
    }

    @Test
    void 학기_명단_밖_회원이_하나라도_있으면_보내기_전에_404다() throws Exception {
        send(key(), body("PAYMENT", ordered(alice.getId(), outsider.getId()), false))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("이 학기 회비 명단에 없는 회원입니다."));
        verifyNoInteractions(slackClient);

        perform(post(URL, "2025-1").header("Idempotency-Key", key()), adminToken,
                body("PAYMENT", Map.of(alice.getId(), "a"), false)).andExpect(status().isNotFound());
        perform(post(URL, "2026-9").header("Idempotency-Key", key()), adminToken,
                body("PAYMENT", Map.of(alice.getId(), "a"), false)).andExpect(status().isBadRequest());
    }

    @Test
    void 요청_키가_없거나_형식이_틀리면_400이다() throws Exception {
        perform(post(URL, "2026-2"), adminToken, body("PAYMENT", Map.of(alice.getId(), "a"), false))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 키(Idempotency-Key)가 없거나 형식이 올바르지 않습니다."));
        send("short", body("PAYMENT", Map.of(alice.getId(), "a"), false)).andExpect(status().isBadRequest());
        send("has space key 1234", body("PAYMENT", Map.of(alice.getId(), "a"), false))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(slackClient);
    }

    // ---------- 중복 발송 방지 ----------

    @Test
    void 같은_키로_다시_보내면_Slack을_다시_부르지_않고_같은_결과다() throws Exception {
        String key = key();
        String body = body("PAYMENT", ordered(alice.getId(), bob.getId()), false);
        send(key, body).andExpect(jsonPath("$.sentCount").value(2));

        send(key, body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(2))
                .andExpect(jsonPath("$.failed", hasSize(0)));

        verify(slackClient, times(2)).postDirectMessage(anyString(), anyString());
        assertThat(batchCount()).isEqualTo(1);
    }

    @Test
    void 부분_실패_뒤_같은_키로_다시_보내면_실패한_회원만_보낸다() throws Exception {
        String key = key();
        String body = body("PAYMENT", ordered(alice.getId(), bob.getId()), false);
        when(slackClient.postDirectMessage(eq("U0BOB00001"), anyString()))
                .thenThrow(new SlackApiException("channel_not_found", null))
                .thenReturn(new SlackPostResult("DBOB", "2.0"));

        send(key, body).andExpect(jsonPath("$.sentCount").value(1)).andExpect(jsonPath("$.failed", hasSize(1)));
        send(key, body)
                .andExpect(jsonPath("$.sentCount").value(2))
                .andExpect(jsonPath("$.failed", hasSize(0)));

        verify(slackClient, times(1)).postDirectMessage(eq("U0ALICE001"), anyString());
        verify(slackClient, times(2)).postDirectMessage(eq("U0BOB00001"), anyString());
    }

    @Test
    void 같은_키로_다른_내용을_보내면_409다() throws Exception {
        String key = key();
        send(key, body("PAYMENT", Map.of(alice.getId(), "원래 본문"), false)).andExpect(status().isOk());

        send(key, body("PAYMENT", Map.of(alice.getId(), "바뀐 본문"), false))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 다른 내용으로 보낸 요청 키입니다. 새로 보내려면 다시 시도하세요."));
        send(key, body("UNPAID", Map.of(alice.getId(), "원래 본문"), false)).andExpect(status().isConflict());
        send(key, body("PAYMENT", ordered(alice.getId(), bob.getId()), false)).andExpect(status().isConflict());
        verify(slackClient, times(1)).postDirectMessage(anyString(), anyString());
    }

    @Test
    void 같은_키가_보내는_중이면_409이고_오래_멈춘_발송은_이어서_보낸다() throws Exception {
        String key = key();
        String body = body("PAYMENT", Map.of(alice.getId(), "안내"), false);
        insertInProgressBatch(key, body, "now()");

        send(key, body)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("같은 알림을 보내는 중입니다. 잠시 뒤 결과를 확인하세요."));
        verifyNoInteractions(slackClient);

        jdbcTemplate.update("UPDATE dues_notification_batch SET updated_at = now() - interval '2 minutes'");
        send(key, body).andExpect(status().isOk()).andExpect(jsonPath("$.sentCount").value(1));
    }

    @Test
    void 같은_키를_동시에_보내면_하나는_409다() throws Exception {
        String key = key();
        String body = body("PAYMENT", Map.of(alice.getId(), "안내"), false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(slackClient.postDirectMessage(anyString(), anyString())).thenAnswer(invocation -> {
            entered.countDown();
            release.await(10, TimeUnit.SECONDS);
            return new SlackPostResult("D1", "1.0");
        });

        CompletableFuture<Integer> first = CompletableFuture.supplyAsync(() -> statusOf(key, body));
        assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue();
        int second = statusOf(key, body);
        release.countDown();

        assertThat(second).isEqualTo(409);
        assertThat(first.get(10, TimeUnit.SECONDS)).isEqualTo(200);
        verify(slackClient, times(1)).postDirectMessage(anyString(), anyString());
    }

    @Test
    void 보낸_회원은_즉시_기록돼_중간에_멈춰도_다시_보내지_않는다() throws Exception {
        String key = key();
        String body = body("PAYMENT", ordered(alice.getId(), bob.getId(), carol.getId()), false);
        when(slackClient.postDirectMessage(eq("U0CAROL001"), anyString()))
                .thenThrow(new AssertionError("서버가 중간에 멈춘 상황"))
                .thenReturn(new SlackPostResult("DCAROL", "3.0"));

        try {
            send(key, body);
        } catch (Exception | AssertionError ignored) {
            // 요청 처리 중 예외 — 앞의 두 회원은 이미 커밋돼 있어야 한다.
        }
        assertThat(jdbcTemplate.queryForList(
                "SELECT status FROM dues_notification_recipient ORDER BY member_id", String.class))
                .containsExactly("SENT", "SENT", "PENDING");

        jdbcTemplate.update("UPDATE dues_notification_batch SET updated_at = now() - interval '2 minutes'");
        send(key, body).andExpect(jsonPath("$.sentCount").value(3));
        verify(slackClient, times(1)).postDirectMessage(eq("U0ALICE001"), anyString());
        verify(slackClient, times(1)).postDirectMessage(eq("U0BOB00001"), anyString());
    }

    // ---------- 시간 예산·429 ----------

    @Test
    void 시간_예산_20초를_넘기면_남은_회원은_보내지_않고_같은_키로_다시_보내면_그_회원만_보낸다() throws Exception {
        String key = key();
        String body = body("PAYMENT", ordered(alice.getId(), bob.getId(), carol.getId()), false);
        Instant start = Instant.parse("2026-10-10T03:00:00Z");
        clock.setInstant(start);
        when(slackClient.postDirectMessage(eq("U0ALICE001"), anyString())).thenAnswer(invocation -> {
            clock.setInstant(start.plusSeconds(21));
            return new SlackPostResult("DALICE", "1.0");
        });

        send(key, body)
                .andExpect(jsonPath("$.sentCount").value(1))
                .andExpect(jsonPath("$.failed[*].memberId").value(contains(bob.getId().intValue(), carol.getId().intValue())))
                .andExpect(jsonPath("$.failed[*].reason").value(contains("SLACK_ERROR", "SLACK_ERROR")));
        verify(slackClient, never()).postDirectMessage(eq("U0BOB00001"), anyString());

        send(key, body).andExpect(jsonPath("$.sentCount").value(3));
        verify(slackClient, times(1)).postDirectMessage(eq("U0ALICE001"), anyString());
        verify(slackClient, times(1)).postDirectMessage(eq("U0BOB00001"), anyString());
    }

    @Test
    void 레이트_리밋이면_Retry_After가_예산_안일_때_한_번만_다시_보낸다() throws Exception {
        when(slackClient.postDirectMessage(eq("U0ALICE001"), anyString()))
                .thenThrow(new SlackApiException("ratelimited", 0L))
                .thenReturn(new SlackPostResult("DALICE", "1.0"));
        when(slackClient.postDirectMessage(eq("U0BOB00001"), anyString()))
                .thenThrow(new SlackApiException("ratelimited", 30L));

        send(key(), body("PAYMENT", ordered(alice.getId(), bob.getId()), false))
                .andExpect(jsonPath("$.sentCount").value(1))
                .andExpect(jsonPath("$.failed[0].memberId").value(bob.getId().intValue()));

        verify(slackClient, times(2)).postDirectMessage(eq("U0ALICE001"), anyString());
        verify(slackClient, times(1)).postDirectMessage(eq("U0BOB00001"), anyString());
    }

    // ---------- 나에게 테스트 발송 ----------

    @Test
    void 나에게_테스트_발송은_첫_본문만_관리자에게_1건_보내고_집계에_섞이지_않는다() throws Exception {
        send(null, body("UNPAID", ordered(alice.getId(), bob.getId()), true, "첫 본문 {담당자 멘션}", "둘째 본문"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(1))
                .andExpect(jsonPath("$.failed", hasSize(0)));

        verify(slackClient).postDirectMessage(ADMIN_SLACK, "첫 본문 <@U0ADMIN001>");
        verify(slackClient, times(1)).postDirectMessage(anyString(), anyString());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT test_to_self || ':' || (idempotency_key IS NULL) || ':' || recipient_count FROM dues_notification_batch",
                String.class)).isEqualTo("true:true:1");
        assertThat(jdbcTemplate.queryForList("SELECT member_id FROM dues_notification_recipient", Long.class))
                .containsExactly(adminId);

        // 테스트 발송은 요청 키와 무관하고 다시 누르면 다시 보낸다. 실제 발송의 중복 판정에도 영향이 없다.
        String key = key();
        send(key, body("UNPAID", ordered(alice.getId(), bob.getId()), true, "첫 본문 {담당자 멘션}", "둘째 본문"))
                .andExpect(jsonPath("$.sentCount").value(1));
        send(key, body("UNPAID", ordered(alice.getId(), bob.getId()), false, "첫 본문 {담당자 멘션}", "둘째 본문"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(2));
        verify(slackClient, times(1)).postDirectMessage(eq("U0ALICE001"), anyString());
    }

    @Test
    void 테스트_발송인데_관리자_Slack_ID가_없으면_400이다() throws Exception {
        jdbcTemplate.update("UPDATE member SET slack_id = NULL WHERE id = ?", adminId);

        send(null, body("PAYMENT", Map.of(alice.getId(), "안내"), true))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "내 Slack ID가 인명부에 없어 테스트 발송을 할 수 없습니다. 인명부에서 Slack ID를 먼저 저장하세요."));
        verifyNoInteractions(slackClient);
    }

    @Test
    void 테스트_발송도_명단을_검사하고_실패하면_관리자_id로_돌려준다() throws Exception {
        send(null, body("PAYMENT", Map.of(outsider.getId(), "안내"), true)).andExpect(status().isNotFound());

        when(slackClient.postDirectMessage(eq(ADMIN_SLACK), anyString()))
                .thenThrow(new SlackApiException("missing_scope", null));
        send(null, body("PAYMENT", Map.of(alice.getId(), "안내"), true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sentCount").value(0))
                .andExpect(jsonPath("$.failed[0].memberId").value(adminId.intValue()))
                .andExpect(jsonPath("$.failed[0].reason").value("SLACK_ERROR"));
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        String memberToken = login("20240001");

        perform(post(URL, "2026-2").header("Idempotency-Key", key()), memberToken,
                body("PAYMENT", Map.of(alice.getId(), "안내"), false))
                .andExpect(status().isForbidden());
        verifyNoInteractions(slackClient);
    }

    // ---------- helpers ----------

    private void setSlackId(Member member, String slackId) {
        jdbcTemplate.update("UPDATE member SET slack_id = ? WHERE id = ?", slackId, member.getId());
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }

    private static Map<Long, String> ordered(Long... memberIds) {
        Map<Long, String> map = new java.util.LinkedHashMap<>();
        for (Long memberId : memberIds) {
            map.put(memberId, "안내 " + memberId);
        }
        return map;
    }

    private String body(String type, Map<Long, String> messages, boolean testToSelf) {
        ObjectNode node = objectMapper.createObjectNode().put("type", type).put("testToSelf", testToSelf);
        ArrayNode array = node.putArray("messages");
        new java.util.TreeMap<>(messages).forEach((memberId, message) ->
                array.addObject().put("memberId", memberId).put("message", message));
        return node.toString();
    }

    private String body(String type, Map<Long, String> members, boolean testToSelf, String... texts) {
        ObjectNode node = objectMapper.createObjectNode().put("type", type).put("testToSelf", testToSelf);
        ArrayNode array = node.putArray("messages");
        List<Long> ids = List.copyOf(new java.util.TreeMap<>(members).keySet());
        for (int i = 0; i < ids.size(); i++) {
            array.addObject().put("memberId", ids.get(i)).put("message", texts[i]);
        }
        return node.toString();
    }

    private ResultActions send(String idempotencyKey, String body) throws Exception {
        var request = post(URL, "2026-2");
        if (idempotencyKey != null) {
            request.header("Idempotency-Key", idempotencyKey);
        }
        return perform(request, adminToken, body);
    }

    private int statusOf(String key, String body) {
        try {
            return send(key, body).andReturn().getResponse().getStatus();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** 다른 요청이 같은 키로 보내는 중인 상태를 만든다. 요청 해시는 서비스와 같은 방식으로 계산한다. */
    private void insertInProgressBatch(String key, String body, String updatedAtSql) throws Exception {
        var request = objectMapper.readValue(body,
                com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesNotificationRequest.class);
        Long semesterId = semesterIdOf("2026-2");
        String hash = (String) invokeRequestHash(request);
        jdbcTemplate.update("""
                INSERT INTO dues_notification_batch (idempotency_key, request_hash, semester_id, type, status,
                                                     requested_by, recipient_count, updated_at)
                VALUES (?, ?, ?, 'PAYMENT', 'IN_PROGRESS', ?, 1, %s)""".formatted(updatedAtSql),
                key, hash, semesterId, adminId);
        Long batchId = jdbcTemplate.queryForObject("SELECT id FROM dues_notification_batch WHERE idempotency_key = ?",
                Long.class, key);
        jdbcTemplate.update("INSERT INTO dues_notification_recipient (batch_id, member_id, status) VALUES (?, ?, 'PENDING')",
                batchId, alice.getId());
    }

    private static Object invokeRequestHash(Object request) throws Exception {
        var method = com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesNotificationService.class
                .getDeclaredMethod("requestHash", request.getClass());
        method.setAccessible(true);
        return method.invoke(null, request);
    }

    private long batchCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM dues_notification_batch", Long.class);
    }
}
