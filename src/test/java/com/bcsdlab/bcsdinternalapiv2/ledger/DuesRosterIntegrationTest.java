package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesSemesterWriter;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

class DuesRosterIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String ROSTER = "/v1/admin/dues/semesters/{semesterId}/roster";

    @Autowired
    private DuesSemesterWriter semesterWriter;

    // ---------- GET 명단 ----------

    @Test
    void 명단은_이름_학번_member_id_순이고_납부_대상_여부를_담는다() throws Exception {
        member("20240003", "나회원").save();
        member("20240002", "가회원").duesRequired(false).save();
        member("20240001", "가회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        perform(get(ROSTER, "2026-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[*].studentNumber").value(contains("20240001", "20240002", "20240003")))
                .andExpect(jsonPath("$.members[*].applicable").value(contains(true, false, true)))
                .andExpect(jsonPath("$.members[0].name").value("가회원"))
                .andExpect(jsonPath("$.members[0].track").value("BACKEND"));
    }

    @Test
    void 학기가_없으면_404_형식이_틀리면_400이다() throws Exception {
        perform(get(ROSTER, "2026-2")).andExpect(status().isNotFound());
        perform(get(ROSTER + "/candidates", "2026-2")).andExpect(status().isNotFound());
        perform(get(ROSTER, "2026-9")).andExpect(status().isBadRequest());
        perform(post(ROSTER, "2026-2"), adminToken, "{\"memberId\":1,\"applicable\":true}")
                .andExpect(status().isNotFound());
        perform(patch(ROSTER + "/{memberId}", "2026-2", 1), adminToken, "{\"applicable\":true}")
                .andExpect(status().isNotFound());
    }

    // ---------- GET 후보 ----------

    @Test
    void 후보는_명단에_없는_모든_회원이다_비활동_탈퇴_포함() throws Exception {
        Member onRoster = member("20240001", "명단회원").save();
        Member inactive = member("20240002", "비활동").clubActive(false).save();
        Member withdrawn = member("20240003", "탈퇴").status(MemberStatus.WITHDRAWN).save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        Member joinedLater = member("20240004", "학기중가입").save();

        perform(get(ROSTER + "/candidates", "2026-2"))
                .andExpect(status().isOk())
                // 관리자(비활동)도 명단에 없으므로 후보다.
                .andExpect(jsonPath("$.members[*].name").value(contains("관리자", "비활동", "탈퇴", "학기중가입")))
                .andExpect(jsonPath("$.members[*].memberId").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(onRoster.getId().intValue()))))
                .andExpect(jsonPath("$.members[1].memberId").value(inactive.getId().intValue()))
                .andExpect(jsonPath("$.members[1].studentNumber").value("20240002"))
                .andExpect(jsonPath("$.members[1].track").value("BACKEND"))
                .andExpect(jsonPath("$.members[1].applicable").doesNotExist());

        addMember("2026-2", withdrawn.getId(), true).andExpect(status().isCreated());
        addMember("2026-2", joinedLater.getId(), false).andExpect(status().isCreated());

        perform(get(ROSTER + "/candidates", "2026-2"))
                .andExpect(jsonPath("$.members[*].name").value(contains("관리자", "비활동")));
    }

    // ---------- POST 추가 ----------

    @Test
    void 명단에_추가하면_201과_명단_행을_돌려주고_학기_상세에_반영된다() throws Exception {
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        Member joined = member("20240009", "신입").duesRequired(false).save();

        addMember("2026-2", joined.getId(), true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(joined.getId().intValue()))
                .andExpect(jsonPath("$.name").value("신입"))
                .andExpect(jsonPath("$.studentNumber").value("20240009"))
                .andExpect(jsonPath("$.track").value("BACKEND"))
                .andExpect(jsonPath("$.applicable").value(true));

        perform(get("/v1/admin/dues/semesters/2026-2/members"))
                .andExpect(jsonPath("$.semester.totalMembers").value(1))
                .andExpect(jsonPath("$.members[0].memberId").value(joined.getId().intValue()))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(60_000));
    }

    @Test
    void 이미_명단에_있으면_409다() throws Exception {
        Member member = member("20240001", "회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        addMember("2026-2", member.getId(), false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 이 학기 명단에 있는 회원입니다."));
    }

    @Test
    void 없는_회원이면_404다() throws Exception {
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        addMember("2026-2", 999_999L, true)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
    }

    @Test
    void 본문이_비면_400이다() throws Exception {
        Member member = member("20240001", "회원").clubActive(false).save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        perform(post(ROSTER, "2026-2"), adminToken, "{\"memberId\":%d}".formatted(member.getId()))
                .andExpect(status().isBadRequest());
        perform(post(ROSTER, "2026-2"), adminToken, "{\"applicable\":true}")
                .andExpect(status().isBadRequest());
        perform(patch(ROSTER + "/{memberId}", "2026-2", member.getId()), adminToken, "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void 저장_단계의_PK_위반은_덮어쓰지_않고_409로_바꾼다() throws Exception {
        Member member = member("20240001", "회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        Long semesterId = jdbcTemplate.queryForObject("SELECT id FROM dues_semester", Long.class);

        // 사전 확인을 건너뛰고 저장만 한다. merge로 처리되면 applicable이 조용히 false로 바뀐다.
        assertThatThrownBy(() -> semesterWriter.insertRosterRow(semesterId, member, false))
                .isInstanceOf(LedgerException.class)
                .extracting(e -> ((LedgerException) e).getExceptionType())
                .isEqualTo(LedgerExceptionType.ROSTER_MEMBER_EXISTS);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT applicable FROM dues_semester_member WHERE member_id = ?", Boolean.class, member.getId()))
                .isTrue();
    }

    // ---------- PATCH 정정 ----------

    @Test
    void 납부_비대상으로_바꾸면_학기_상세가_비대상_모양이_되고_다시_대상으로_돌릴_수_있다() throws Exception {
        Member member = member("20240001", "회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        updateMember("2026-2", member.getId(), false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(member.getId().intValue()))
                .andExpect(jsonPath("$.applicable").value(false));

        perform(get("/v1/admin/dues/semesters/2026-2/members"))
                .andExpect(jsonPath("$.members[0].status").value("EXEMPT"))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(nullValue()))
                .andExpect(jsonPath("$.members[0].paidAmount").value(nullValue()))
                .andExpect(jsonPath("$.members[0].unpaidAmount").value(nullValue()))
                .andExpect(jsonPath("$.members[0].excessAmount").value(0))
                .andExpect(jsonPath("$.members[0].months[*].status").value(everyItem(is("NOT_APPLICABLE"))))
                .andExpect(jsonPath("$.semester.totalAmount").value(0));
        perform(get(ROSTER, "2026-2")).andExpect(jsonPath("$.members[0].applicable").value(false));

        updateMember("2026-2", member.getId(), true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicable").value(true));
        perform(get("/v1/admin/dues/semesters/2026-2/members"))
                .andExpect(jsonPath("$.members[0].status").value("UNPAID"));
    }

    @Test
    void 명단에_없는_회원을_정정하면_404다() throws Exception {
        Member outsider = member("20240001", "명단밖").clubActive(false).save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        updateMember("2026-2", outsider.getId(), false)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("이 학기 회비 명단에 없는 회원입니다."));
    }

    // ---------- 지난 학기 (마감 없음) ----------

    @Test
    void 지난_학기_명단도_추가하고_정정할_수_있다() throws Exception {
        Member early = member("20240001", "기존").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        clock.setToday(LocalDate.of(2027, 4, 1));
        createSemester(2027, 1, 10_000).andExpect(status().isCreated());
        Member late = member("20240002", "뒤늦은등록").clubActive(false).save();

        addMember("2026-2", late.getId(), true).andExpect(status().isCreated());
        updateMember("2026-2", early.getId(), false).andExpect(status().isOk());

        perform(get(ROSTER, "2026-2"))
                .andExpect(jsonPath("$.members", hasSize(2)))
                .andExpect(jsonPath("$.members[*].applicable").value(contains(false, true)));
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        Member member = member("20240001", "일반").save();
        String memberToken = login("20240001");
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());

        perform(get(ROSTER, "2026-2"), memberToken).andExpect(status().isForbidden());
        perform(get(ROSTER + "/candidates", "2026-2"), memberToken).andExpect(status().isForbidden());
        perform(post(ROSTER, "2026-2"), memberToken, "{\"memberId\":%d,\"applicable\":true}".formatted(member.getId()))
                .andExpect(status().isForbidden());
        perform(patch(ROSTER + "/{memberId}", "2026-2", member.getId()), memberToken, "{\"applicable\":false}")
                .andExpect(status().isForbidden());
    }

    private ResultActions addMember(String semesterId, Long memberId, boolean applicable) throws Exception {
        return perform(post(ROSTER, semesterId), adminToken,
                "{\"memberId\":%d,\"applicable\":%b}".formatted(memberId, applicable));
    }

    private ResultActions updateMember(String semesterId, Long memberId, boolean applicable) throws Exception {
        return perform(patch(ROSTER + "/{memberId}", semesterId, memberId), adminToken,
                "{\"applicable\":%b}".formatted(applicable));
    }
}
