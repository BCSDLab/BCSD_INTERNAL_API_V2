package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesLinkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesLinkService;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesLinkWriter;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.RosterService;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

class DuesLinkIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String ENTRIES = "/v1/admin/ledger/entries";
    private static final String DETAIL = "/v1/admin/dues/semesters/2026-2/members";

    @Autowired
    private DuesLinkWriter linkWriter;

    @Autowired
    private DuesLinkService linkService;

    @Autowired
    private RosterService rosterService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Member alice;
    private Member bob;
    private Member notApplicable;
    private Member outsider;

    @BeforeEach
    void setUpRoster() throws Exception {
        alice = member("20240001", "가회원").save();
        bob = member("20240002", "나회원").save();
        notApplicable = member("20240003", "다비대상").duesRequired(false).save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        outsider = member("20240004", "라명단밖").save();
    }

    // ---------- PUT 단건 연결 ----------

    @Test
    void 입금을_연결하면_DUES가_되고_CONFIRMED이며_집계에_반영된다() throws Exception {
        LedgerEntry entry = deposit(60_000);

        link(entry.getId(), alice.getId(), "2026-2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("DUES"))
                .andExpect(jsonPath("$.linkStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.duesLink.memberId").value(alice.getId().intValue()))
                .andExpect(jsonPath("$.duesLink.semesterId").value("2026-2"));

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].status").value("PAID"))
                .andExpect(jsonPath("$.members[0].paidAmount").value(60_000));
    }

    @Test
    void 출금을_연결해도_DUES이고_미납으로_바뀌며_상한이_없다() throws Exception {
        link(deposit(60_000).getId(), alice.getId(), "2026-2").andExpect(status().isOk());

        link(withdrawal(15_000).getId(), alice.getId(), "2026-2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.category").value("DUES"));
        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].status").value("UNPAID"))
                .andExpect(jsonPath("$.members[0].paidAmount").value(45_000))
                .andExpect(jsonPath("$.members[0].unpaidAmount").value(15_000))
                .andExpect(jsonPath("$.members[0].months[3].status").value("PAID"))
                .andExpect(jsonPath("$.members[0].months[4].status").value("UNPAID"))
                .andExpect(jsonPath("$.members[0].months[4].note").value("5,000원 부분 납부"));

        // 입금보다 큰 출금도 막지 않는다(반환 상한 없음).
        link(withdrawal(100_000).getId(), alice.getId(), "2026-2").andExpect(status().isOk());
        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].paidAmount").value(-55_000))
                .andExpect(jsonPath("$.members[0].unpaidAmount").value(115_000))
                .andExpect(jsonPath("$.semester.paidAmount").value(0))
                .andExpect(jsonPath("$.semester.unpaidMembers").value(2));
    }

    @Test
    void 다른_회원으로_옮기면_기존_회원의_집계에서_빠지고_기존_회원은_비대상으로_바꿀_수_있다() throws Exception {
        LedgerEntry entry = deposit(60_000);
        link(entry.getId(), alice.getId(), "2026-2").andExpect(status().isOk());

        link(entry.getId(), bob.getId(), "2026-2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duesLink.memberId").value(bob.getId().intValue()));

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].paidAmount").value(0))
                .andExpect(jsonPath("$.members[1].paidAmount").value(60_000));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dues_link", Long.class)).isEqualTo(1);
        updateRoster(alice.getId(), false).andExpect(status().isOk());
        updateRoster(bob.getId(), false).andExpect(status().isConflict());
    }

    @Test
    void 다른_학기로도_옮길_수_있다() throws Exception {
        clock.setToday(java.time.LocalDate.of(2027, 4, 1));
        createSemester(2027, 1, 10_000).andExpect(status().isCreated());
        LedgerEntry entry = deposit(10_000);
        link(entry.getId(), alice.getId(), "2026-2").andExpect(status().isOk());

        link(entry.getId(), alice.getId(), "2027-1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duesLink.semesterId").value("2027-1"));
        perform(get(DETAIL)).andExpect(jsonPath("$.members[0].paidAmount").value(0));
    }

    @Test
    void 비대상_회원에게_연결하면_409이고_기존_연결은_그대로다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        link(entry.getId(), alice.getId(), "2026-2").andExpect(status().isOk());

        link(entry.getId(), notApplicable.getId(), "2026-2")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이 학기 납부 비대상 회원에게는 입출금 내역을 연결할 수 없습니다."));
        perform(get(ENTRIES)).andExpect(jsonPath("$.entries[0].duesLink.memberId").value(alice.getId().intValue()));
    }

    @Test
    void 명단_밖_회원이면_404_없는_내역_학기면_404_학기_형식이_틀리면_400() throws Exception {
        LedgerEntry entry = deposit(10_000);

        link(entry.getId(), outsider.getId(), "2026-2")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("이 학기 회비 명단에 없는 회원입니다."));
        link(999_999L, alice.getId(), "2026-2")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("장부 기록을 찾을 수 없습니다."));
        link(entry.getId(), alice.getId(), "2025-1").andExpect(status().isNotFound());
        link(entry.getId(), alice.getId(), "2026-5").andExpect(status().isBadRequest());
        perform(put(ENTRIES + "/{id}/dues-link", entry.getId()), adminToken, "{\"semesterId\":\"2026-2\"}")
                .andExpect(status().isBadRequest());
        perform(get(ENTRIES)).andExpect(jsonPath("$.entries[0].category").value("ETC"));
    }

    // ---------- DELETE 해제 ----------

    @Test
    void 해제하면_분류는_그대로_PENDING이고_다시_해제해도_204다() throws Exception {
        LedgerEntry entry = deposit(60_000);
        link(entry.getId(), alice.getId(), "2026-2").andExpect(status().isOk());

        perform(delete(ENTRIES + "/{id}/dues-link", entry.getId())).andExpect(status().isNoContent());
        perform(delete(ENTRIES + "/{id}/dues-link", entry.getId())).andExpect(status().isNoContent());

        perform(get(ENTRIES))
                .andExpect(jsonPath("$.entries[0].category").value("DUES"))
                .andExpect(jsonPath("$.entries[0].linkStatus").value("PENDING"))
                .andExpect(jsonPath("$.entries[0].duesLink").value(nullValue()));
        perform(get(DETAIL)).andExpect(jsonPath("$.members[0].paidAmount").value(0));
    }

    @Test
    void 없는_내역을_해제하면_404다() throws Exception {
        perform(delete(ENTRIES + "/{id}/dues-link", 999_999L)).andExpect(status().isNotFound());
    }

    // ---------- POST 일괄 연결 ----------

    @Test
    void 일괄_연결은_없는_내역과_이미_연결된_내역을_건너뛰고_센다() throws Exception {
        LedgerEntry first = deposit(60_000);
        LedgerEntry second = withdrawal(10_000);
        LedgerEntry alreadyLinked = deposit(10_000);
        link(alreadyLinked.getId(), bob.getId(), "2026-2").andExpect(status().isOk());

        bulk(Map.of(first.getId(), alice.getId()), Map.of(second.getId(), alice.getId()),
                Map.of(alreadyLinked.getId(), alice.getId()), Map.of(999_999L, alice.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkedCount").value(2));

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].paidAmount").value(50_000))
                .andExpect(jsonPath("$.members[1].paidAmount").value(10_000));
        perform(get(ENTRIES)).andExpect(jsonPath("$.entries[?(@.id == %d)].category".formatted(second.getId()))
                .value("DUES"));
    }

    @Test
    void 일괄_연결에_명단_밖_회원이_하나라도_있으면_404이고_아무것도_연결하지_않는다() throws Exception {
        LedgerEntry first = deposit(60_000);
        LedgerEntry second = deposit(10_000);

        bulk(Map.of(first.getId(), alice.getId()), Map.of(second.getId(), outsider.getId()))
                .andExpect(status().isNotFound());

        assertNothingLinked();
    }

    @Test
    void 일괄_연결에_비대상_회원이_하나라도_있으면_409이고_아무것도_연결하지_않는다() throws Exception {
        LedgerEntry first = deposit(60_000);
        LedgerEntry second = deposit(10_000);

        bulk(Map.of(first.getId(), alice.getId()), Map.of(second.getId(), notApplicable.getId()))
                .andExpect(status().isConflict());

        assertNothingLinked();
    }

    @Test
    void 건너뛸_내역의_회원도_검사하고_요청_순서상_처음_실패한_회원이_오류를_정한다() throws Exception {
        bulk(Map.of(999_998L, notApplicable.getId()), Map.of(999_999L, outsider.getId()))
                .andExpect(status().isConflict());
        bulk(Map.of(999_998L, outsider.getId()), Map.of(999_999L, notApplicable.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void 일괄_요청_안에_같은_내역이_두_번이면_400이다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        bulk(Map.of(entry.getId(), alice.getId()), Map.of(entry.getId(), bob.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("같은 장부 기록이 요청에 두 번 있습니다."));
        perform(post("/v1/admin/dues/semesters/2026-2/links"), adminToken, "{\"links\":[]}")
                .andExpect(status().isBadRequest());
        assertNothingLinked();
    }

    @Test
    void 일괄_연결_학기가_없으면_404다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        perform(post("/v1/admin/dues/semesters/2025-1/links"), adminToken,
                "{\"links\":[{\"entryId\":%d,\"memberId\":%d}]}".formatted(entry.getId(), alice.getId()))
                .andExpect(status().isNotFound());
    }

    // ---------- 명단 PATCH와 연결 ----------

    @Test
    void 연결이_있는_회원을_비대상으로_바꾸면_409이고_해제한_뒤에는_된다() throws Exception {
        LedgerEntry entry = deposit(60_000);
        link(entry.getId(), alice.getId(), "2026-2").andExpect(status().isOk());

        updateRoster(alice.getId(), false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("연결된 입출금 내역이 있어 납부 비대상으로 바꿀 수 없습니다. 연결을 먼저 해제하세요."));
        updateRoster(alice.getId(), true).andExpect(status().isOk());

        perform(delete(ENTRIES + "/{id}/dues-link", entry.getId())).andExpect(status().isNoContent());
        updateRoster(alice.getId(), false).andExpect(status().isOk());
    }

    @Test
    void 연결이_명단_행을_잡고_있으면_비대상_전환은_기다렸다가_409를_받는다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        Long semesterId = semesterIdOf("2026-2");
        CountDownLatch linked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        CompletableFuture<Void> linker = CompletableFuture.runAsync(() -> transactionTemplate.executeWithoutResult(s -> {
            linkService.link(entry.getId(), new DuesLinkRequest(alice.getId(), "2026-2"), null);
            linked.countDown();
            await(release);
        }));
        assertThat(linked.await(10, TimeUnit.SECONDS)).isTrue();

        CompletableFuture<Integer> patch = CompletableFuture.supplyAsync(() -> statusOf(() ->
                updateRoster(alice.getId(), false)));
        Thread.sleep(500);
        assertThat(patch).as("명단 행 FOR SHARE에 막혀 기다려야 한다").isNotDone();

        release.countDown();
        linker.get(10, TimeUnit.SECONDS);
        assertThat(patch.get(10, TimeUnit.SECONDS)).isEqualTo(409);
        assertThat(applicable(semesterId, alice.getId())).isTrue();
    }

    @Test
    void 비대상_전환이_명단_행을_잡고_있으면_연결은_기다렸다가_409를_받는다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        CountDownLatch updated = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        CompletableFuture<Void> updater = CompletableFuture.runAsync(() -> transactionTemplate.executeWithoutResult(s -> {
            rosterService.update("2026-2", alice.getId(), new RosterUpdateRequest(false));
            updated.countDown();
            await(release);
        }));
        assertThat(updated.await(10, TimeUnit.SECONDS)).isTrue();

        CompletableFuture<Integer> linkCall = CompletableFuture.supplyAsync(() -> statusOf(() ->
                link(entry.getId(), alice.getId(), "2026-2")));
        Thread.sleep(500);
        assertThat(linkCall).as("명단 행 FOR UPDATE에 막혀 기다려야 한다").isNotDone();

        release.countDown();
        updater.get(10, TimeUnit.SECONDS);
        assertThat(linkCall.get(10, TimeUnit.SECONDS)).isEqualTo(409);
        assertNothingLinked();
    }

    // ---------- 저장 단계 제약 위반 ----------

    @Test
    void 저장_단계의_dues_link_PK_위반은_409로_바꾸고_기존_연결은_그대로다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        link(entry.getId(), alice.getId(), "2026-2").andExpect(status().isOk());
        Long semesterId = semesterIdOf("2026-2");

        // 내역 잠금과 기존 연결 확인을 건너뛰고 INSERT만 해서, 확인과 저장 사이에 다른 요청이 끼어든 경우를 만든다.
        assertThatThrownBy(() -> linkWriter.insert(entry.getId(), semesterId, bob.getId(), null, Instant.now()))
                .isInstanceOf(LedgerException.class)
                .extracting(e -> ((LedgerException) e).getExceptionType())
                .isEqualTo(LedgerExceptionType.ENTRY_ALREADY_LINKED);
        assertThat(jdbcTemplate.queryForObject("SELECT member_id FROM dues_link WHERE entry_id = ?", Long.class,
                entry.getId())).isEqualTo(alice.getId());
    }

    @Test
    void 저장_단계의_명단_FK_위반은_404로_바꾼다() {
        LedgerEntry entry = deposit(10_000);
        Long semesterId = semesterIdOf("2026-2");

        assertThatThrownBy(() -> linkWriter.insert(entry.getId(), semesterId, outsider.getId(), null, Instant.now()))
                .isInstanceOf(LedgerException.class)
                .extracting(e -> ((LedgerException) e).getExceptionType())
                .isEqualTo(LedgerExceptionType.NOT_ROSTER_MEMBER);
        assertNothingLinked();
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        String memberToken = login("20240001");
        LedgerEntry entry = deposit(10_000);

        perform(put(ENTRIES + "/{id}/dues-link", entry.getId()), memberToken,
                "{\"memberId\":%d,\"semesterId\":\"2026-2\"}".formatted(alice.getId()))
                .andExpect(status().isForbidden());
        perform(delete(ENTRIES + "/{id}/dues-link", entry.getId()), memberToken).andExpect(status().isForbidden());
        perform(post("/v1/admin/dues/semesters/2026-2/links"), memberToken,
                "{\"links\":[{\"entryId\":%d,\"memberId\":%d}]}".formatted(entry.getId(), alice.getId()))
                .andExpect(status().isForbidden());
        assertNothingLinked();
    }

    private void assertNothingLinked() {
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dues_link", Long.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ledger_entry WHERE category = 'DUES'",
                Long.class)).isZero();
    }

    private Boolean applicable(Long semesterId, Long memberId) {
        return jdbcTemplate.queryForObject(
                "SELECT applicable FROM dues_semester_member WHERE semester_id = ? AND member_id = ?", Boolean.class,
                semesterId, memberId);
    }

    private ResultActions link(Long entryId, Long memberId, String semesterId) throws Exception {
        return perform(put(ENTRIES + "/{id}/dues-link", entryId), adminToken,
                "{\"memberId\":%d,\"semesterId\":\"%s\"}".formatted(memberId, semesterId));
    }

    private ResultActions updateRoster(Long memberId, boolean applicable) throws Exception {
        return perform(patch("/v1/admin/dues/semesters/2026-2/roster/{memberId}", memberId), adminToken,
                "{\"applicable\":%b}".formatted(applicable));
    }

    @SafeVarargs
    private ResultActions bulk(Map<Long, Long>... links) throws Exception {
        String items = java.util.Arrays.stream(links)
                .map(link -> link.entrySet().iterator().next())
                .map(link -> "{\"entryId\":%d,\"memberId\":%d}".formatted(link.getKey(), link.getValue()))
                .collect(java.util.stream.Collectors.joining(","));
        return perform(post("/v1/admin/dues/semesters/2026-2/links"), adminToken, "{\"links\":[" + items + "]}");
    }

    private interface Call {
        ResultActions run() throws Exception;
    }

    private static int statusOf(Call call) {
        try {
            return call.run().andReturn().getResponse().getStatus();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
