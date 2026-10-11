package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerCategory;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class LedgerEntryIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String ENTRIES = "/v1/admin/ledger/entries";

    // ---------- GET 목록 ----------

    @Test
    void 목록은_occurred_at_occurred_seq_id의_역순이다() throws Exception {
        LocalDateTime same = LocalDateTime.of(2026, 11, 16, 9, 10);
        LedgerEntry older = entry(EntryType.DEPOSIT, LedgerCategory.ETC, 1_000, same.minusDays(1));
        LedgerEntry sameSecondFirst = entry(EntryType.DEPOSIT, LedgerCategory.ETC, 2_000, same);
        LedgerEntry sameSecondLater = ledgerEntryRepository.save(LedgerEntry.builder()
                .occurredAt(same).occurredSeq((short) 1).type(EntryType.WITHDRAWAL).category(LedgerCategory.ETC)
                .counterparty("상대").description("내용").amount(500).bankBalance(1_500).build());
        LedgerEntry newest = entry(EntryType.DEPOSIT, LedgerCategory.ETC, 3_000, same.plusHours(1));

        perform(get(ENTRIES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries[*].id").value(contains(
                        newest.getId().intValue(), sameSecondLater.getId().intValue(),
                        sameSecondFirst.getId().intValue(), older.getId().intValue())));
    }

    @Test
    void 응답_모양_초가_0이어도_생략하지_않고_balance는_은행_잔액이다() throws Exception {
        ledgerEntryRepository.save(LedgerEntry.builder()
                .occurredAt(LocalDateTime.of(2026, 11, 16, 9, 10, 0)).type(EntryType.DEPOSIT)
                .category(LedgerCategory.DUES).counterparty("상대").description("내용").note("메모")
                .amount(10_000).bankBalance(123_456).sourceFileName("거래내역.xlsx").build());

        perform(get(ENTRIES))
                .andExpect(jsonPath("$.entries[0].occurredAt").value("2026-11-16T09:10:00"))
                .andExpect(jsonPath("$.entries[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.entries[0].category").value("DUES"))
                .andExpect(jsonPath("$.entries[0].counterparty").value("상대"))
                .andExpect(jsonPath("$.entries[0].description").value("내용"))
                .andExpect(jsonPath("$.entries[0].note").value("메모"))
                .andExpect(jsonPath("$.entries[0].amount").value(10_000))
                .andExpect(jsonPath("$.entries[0].balance").value(123_456))
                .andExpect(jsonPath("$.entries[0].source").value("거래내역.xlsx"))
                .andExpect(jsonPath("$.entries[0].linkStatus").value("PENDING"))
                .andExpect(jsonPath("$.entries[0].duesLink").value(nullValue()))
                .andExpect(jsonPath("$.entries[0].evidences", hasSize(0)));
    }

    @Test
    void 연결된_내역은_CONFIRMED이고_duesLink를_담는다_DUES가_아니면_NONE() throws Exception {
        Member member = member("20240001", "회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        LedgerEntry linked = deposit(10_000);
        LedgerEntry other = entry(EntryType.WITHDRAWAL, LedgerCategory.EVENT, 5_000,
                LocalDateTime.of(2026, 11, 1, 0, 0));
        perform(put(ENTRIES + "/{id}/dues-link", linked.getId()), adminToken,
                "{\"memberId\":%d,\"semesterId\":\"2026-2\"}".formatted(member.getId()))
                .andExpect(status().isOk());

        perform(get(ENTRIES))
                .andExpect(jsonPath("$.entries[0].id").value(linked.getId().intValue()))
                .andExpect(jsonPath("$.entries[0].linkStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.entries[0].duesLink.memberId").value(member.getId().intValue()))
                .andExpect(jsonPath("$.entries[0].duesLink.memberName").value("회원"))
                .andExpect(jsonPath("$.entries[0].duesLink.studentNumber").value("20240001"))
                .andExpect(jsonPath("$.entries[0].duesLink.track").value("BACKEND"))
                .andExpect(jsonPath("$.entries[0].duesLink.semesterId").value("2026-2"))
                .andExpect(jsonPath("$.entries[0].duesLink.requiredAmount").doesNotExist())
                .andExpect(jsonPath("$.entries[1].id").value(other.getId().intValue()))
                .andExpect(jsonPath("$.entries[1].linkStatus").value("NONE"));
    }

    // ---------- PATCH 수정 ----------

    @Test
    void 수정하면_상대_내용_비고를_통째로_바꾸고_앞뒤_공백을_지운다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), "  새 상대  ", "EVENT", "  새 내용 ", "새 비고")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counterparty").value("새 상대"))
                .andExpect(jsonPath("$.description").value("새 내용"))
                .andExpect(jsonPath("$.note").value("새 비고"))
                .andExpect(jsonPath("$.category").value("EVENT"))
                .andExpect(jsonPath("$.amount").value(10_000))
                .andExpect(jsonPath("$.linkStatus").value("NONE"));
    }

    @Test
    void 분류가_같으면_연결을_유지한다() throws Exception {
        LedgerEntry entry = linkedEntry();

        update(entry.getId(), "상대", "DUES", "내용 수정", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.duesLink.semesterId").value("2026-2"));
    }

    @Test
    void DUES에서_다른_분류로_바꾸면_연결이_끊기고_NONE() throws Exception {
        LedgerEntry entry = linkedEntry();

        update(entry.getId(), "상대", "OPERATION", "내용", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkStatus").value("NONE"))
                .andExpect(jsonPath("$.duesLink").value(nullValue()));
        perform(get("/v1/admin/dues/semesters/2026-2/members"))
                .andExpect(jsonPath("$.members[0].paidAmount").value(0));
    }

    @Test
    void 다른_분류에서_DUES로_바꾸면_PENDING() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), "상대", "DUES", "내용", "")
                .andExpect(jsonPath("$.linkStatus").value("PENDING"))
                .andExpect(jsonPath("$.duesLink").value(nullValue()));
    }

    @Test
    void 다른_분류끼리_바꾸면_NONE() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), "상대", "EVENT", "내용", "")
                .andExpect(jsonPath("$.linkStatus").value("NONE"));
    }

    @Test
    void 검증에_실패하면_400이다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), "   ", "ETC", "내용", "").andExpect(status().isBadRequest());
        update(entry.getId(), "상대", "ETC", " ", "").andExpect(status().isBadRequest());
        update(entry.getId(), "가".repeat(101), "ETC", "내용", "").andExpect(status().isBadRequest());
        update(entry.getId(), "상대", "ETC", "가".repeat(201), "").andExpect(status().isBadRequest());
        update(entry.getId(), "상대", "ETC", "내용", "가".repeat(501)).andExpect(status().isBadRequest());
        update(entry.getId(), "상대", "DUES_REFUND", "내용", "").andExpect(status().isBadRequest());
        patchBody(entry.getId(), "{\"counterparty\":\"상대\",\"category\":\"ETC\",\"description\":\"내용\","
                + "\"note\":\"\",\"evidenceIds\":[1,2,3,4,5,6]}").andExpect(status().isBadRequest());
        patchBody(entry.getId(), "{\"counterparty\":\"상대\",\"category\":\"ETC\",\"description\":\"내용\","
                + "\"note\":\"\",\"evidenceIds\":[1,1]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("같은 증빙이 요청에 두 번 있습니다."));
    }

    @Test
    void 공백을_지운_뒤_최대_길이면_통과한다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), " " + "가".repeat(100) + " ", "ETC", "가".repeat(200), "가".repeat(500))
                .andExpect(status().isOk());
    }

    @Test
    void 증빙_업로드_전이라_증빙을_붙이면_404이고_아무것도_바뀌지_않는다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        patchBody(entry.getId(), "{\"counterparty\":\"바뀐상대\",\"category\":\"EVENT\",\"description\":\"내용\","
                + "\"note\":\"\",\"evidenceIds\":[1]}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("증빙을 찾을 수 없습니다."));
        perform(get(ENTRIES)).andExpect(jsonPath("$.entries[0].counterparty").value("테스트상대"));
    }

    @Test
    void 없는_내역이면_404다() throws Exception {
        update(999_999L, "상대", "ETC", "내용", "")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("장부 기록을 찾을 수 없습니다."));
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        member("20240009", "일반").save();
        String memberToken = login("20240009");
        LedgerEntry entry = deposit(10_000);

        perform(get(ENTRIES), memberToken).andExpect(status().isForbidden());
        perform(patch(ENTRIES + "/{id}", entry.getId()), memberToken,
                "{\"counterparty\":\"상대\",\"category\":\"ETC\",\"description\":\"내용\",\"note\":\"\","
                        + "\"evidenceIds\":[]}")
                .andExpect(status().isForbidden());
    }

    private LedgerEntry linkedEntry() throws Exception {
        Member member = member("20240001", "회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        LedgerEntry entry = deposit(10_000);
        perform(put(ENTRIES + "/{id}/dues-link", entry.getId()), adminToken,
                "{\"memberId\":%d,\"semesterId\":\"2026-2\"}".formatted(member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("DUES"));
        return entry;
    }

    private ResultActions update(Long id, String counterparty, String category, String description, String note)
            throws Exception {
        return patchBody(id, objectMapper.writeValueAsString(java.util.Map.of(
                "counterparty", counterparty, "category", category, "description", description, "note", note,
                "evidenceIds", java.util.List.of())));
    }

    private ResultActions patchBody(Long id, String body) throws Exception {
        return perform(patch(ENTRIES + "/{id}", id), adminToken, body);
    }
}
