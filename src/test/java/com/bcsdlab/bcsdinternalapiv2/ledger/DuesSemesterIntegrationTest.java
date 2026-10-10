package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesSemesterWriter;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DuesSemesterIntegrationTest extends LedgerIntegrationTestSupport {

    @Autowired
    private DuesSemesterWriter semesterWriter;

    @Nested
    class 생성_가능_여부 {

        @Test
        void 학기가_없으면_현재_학기를_만들_수_있다() throws Exception {
            perform(get("/v1/admin/dues/semesters/creatable"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentSemesterId").value("2026-2"))
                    .andExpect(jsonPath("$.nextSemester.year").value(2026))
                    .andExpect(jsonPath("$.nextSemester.term").value(2))
                    .andExpect(jsonPath("$.creatable").value(true));
        }

        @Test
        void 현재_학기가_있으면_바로_다음_학기를_만들_수_있다() throws Exception {
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());

            perform(get("/v1/admin/dues/semesters/creatable"))
                    .andExpect(jsonPath("$.currentSemesterId").value("2026-2"))
                    .andExpect(jsonPath("$.nextSemester.year").value(2027))
                    .andExpect(jsonPath("$.nextSemester.term").value(1))
                    .andExpect(jsonPath("$.creatable").value(true));
        }

        @Test
        void 바로_다음_학기까지_있으면_더_만들_수_없다() throws Exception {
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());
            createSemester(2027, 1, 10_000).andExpect(status().isCreated());

            perform(get("/v1/admin/dues/semesters/creatable"))
                    .andExpect(jsonPath("$.nextSemester.year").value(2027))
                    .andExpect(jsonPath("$.nextSemester.term").value(2))
                    .andExpect(jsonPath("$.creatable").value(false));
        }

        @Test
        void 일월은_전년도_2학기로_판단한다() throws Exception {
            clock.setToday(LocalDate.of(2027, 1, 15));

            perform(get("/v1/admin/dues/semesters/creatable"))
                    .andExpect(jsonPath("$.currentSemesterId").value("2026-2"));
        }

        @Test
        void 서울_날짜로_판단한다_UTC로는_2월_말이어도_서울이_3월이면_1학기() throws Exception {
            // 2027-02-28T15:30Z = 서울 2027-03-01 00:30
            clock.setInstant(java.time.Instant.parse("2027-02-28T15:30:00Z"));

            perform(get("/v1/admin/dues/semesters/creatable"))
                    .andExpect(jsonPath("$.currentSemesterId").value("2027-1"));
        }
    }

    @Nested
    class 생성 {

        @Test
        void 생성하면_201과_요약을_돌려주고_활동_중인_미탈퇴_회원을_명단으로_스냅샷한다() throws Exception {
            Member required = member("20240001", "납부대상").save();
            Member notRequired = member("20240002", "납부비대상").duesRequired(false).save();
            Member pending = member("20240003", "초기설정전").status(MemberStatus.PENDING_SETUP).save();
            Member locked = member("20240004", "잠김").status(MemberStatus.LOCKED).save();
            member("20240005", "비활동").clubActive(false).save();
            member("20240006", "탈퇴").status(MemberStatus.WITHDRAWN).save();

            createSemester(2026, 2, 10_000)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("2026-2"))
                    .andExpect(jsonPath("$.year").value(2026))
                    .andExpect(jsonPath("$.term").value(2))
                    .andExpect(jsonPath("$.monthlyAmount").value(10_000))
                    .andExpect(jsonPath("$.totalMembers").value(4))
                    .andExpect(jsonPath("$.exemptMembers").value(1))
                    .andExpect(jsonPath("$.targetMembers").value(3))
                    .andExpect(jsonPath("$.completedMembers").value(0))
                    .andExpect(jsonPath("$.unpaidMembers").value(3))
                    .andExpect(jsonPath("$.totalAmount").value(180_000))
                    .andExpect(jsonPath("$.paidAmount").value(0))
                    .andExpect(jsonPath("$.unpaidAmount").value(180_000))
                    .andExpect(jsonPath("$.needsReview").value(true));

            perform(get("/v1/admin/dues/semesters/2026-2/roster"))
                    .andExpect(jsonPath("$.members[*].memberId").value(containsInAnyOrder(
                            required.getId().intValue(), notRequired.getId().intValue(),
                            pending.getId().intValue(), locked.getId().intValue())));
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT applicable FROM dues_semester_member WHERE member_id = ?", Boolean.class,
                    notRequired.getId())).isFalse();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT created_by FROM dues_semester", Long.class))
                    .isEqualTo(memberRepository.findByStudentNumber("2020000000").orElseThrow().getId());
        }

        @Test
        void 이미_있는_학기면_409이고_생성_불가보다_먼저_판정한다() throws Exception {
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());
            createSemester(2027, 1, 10_000).andExpect(status().isCreated());

            // 2026-2는 nextSemester(2027-2)도 아니지만 이미 있으므로 409다.
            createSemester(2026, 2, 10_000)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("이미 생성된 학기 회비입니다."));
        }

        @Test
        void 다음에_만들_학기가_아니면_400이다() throws Exception {
            createSemester(2027, 1, 10_000)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("현재 학기의 바로 다음 학기까지만 만들 수 있습니다."));
            createSemester(2026, 1, 10_000).andExpect(status().isBadRequest());
        }

        @Test
        void 바로_다음_학기를_넘으면_400이다() throws Exception {
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());
            createSemester(2027, 1, 10_000).andExpect(status().isCreated());

            createSemester(2027, 2, 10_000).andExpect(status().isBadRequest());
        }

        @Test
        void 요청_값이_범위를_벗어나면_400이다() throws Exception {
            createSemester(2026, 2, 0).andExpect(status().isBadRequest());
            createSemester(2026, 2, 1_000_001).andExpect(status().isBadRequest());
            createSemester(2026, 3, 10_000).andExpect(status().isBadRequest());
            createSemester(1999, 2, 10_000).andExpect(status().isBadRequest());
            perform(post("/v1/admin/dues/semesters"), adminToken, "{\"year\":2026,\"term\":2}")
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 월_회비_상한_1000000원은_만들_수_있다() throws Exception {
            createSemester(2026, 2, 1_000_000).andExpect(status().isCreated());
        }

        @Test
        void 저장_단계의_유니크_위반도_409로_바꾼다() throws Exception {
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());

            // 사전 확인을 건너뛰고 저장만 해서, 확인과 저장 사이에 다른 요청이 끼어든 경우를 만든다.
            assertThatThrownBy(() -> semesterWriter.insertSemester(new SemesterKey(2026, 2), 10_000, null))
                    .isInstanceOf(LedgerException.class)
                    .extracting(e -> ((LedgerException) e).getExceptionType())
                    .isEqualTo(LedgerExceptionType.SEMESTER_ALREADY_EXISTS);
        }

        @Test
        void 같은_학기를_동시에_만들면_하나만_201이고_나머지는_409다() throws Exception {
            member("20240001", "회원").save();
            int threads = 4;
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            try {
                List<Future<Integer>> results = new ArrayList<>();
                for (int i = 0; i < threads; i++) {
                    Callable<Integer> task = () -> {
                        start.await();
                        return createSemester(2026, 2, 10_000).andReturn().getResponse().getStatus();
                    };
                    results.add(executor.submit(task));
                }
                start.countDown();
                List<Integer> statuses = new ArrayList<>();
                for (Future<Integer> result : results) {
                    statuses.add(result.get());
                }
                assertThat(statuses).containsOnlyOnce(201);
                assertThat(statuses).filteredOn(code -> code != 201).containsOnly(409);
            } finally {
                executor.shutdownNow();
            }
            assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dues_semester_member", Long.class))
                    .isEqualTo(1);
        }
    }

    @Nested
    class 조회 {

        @Test
        void 목록은_최신_학기부터다() throws Exception {
            perform(get("/v1/admin/dues/semesters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.semesters", hasSize(0)));
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());
            createSemester(2027, 1, 20_000).andExpect(status().isCreated());

            perform(get("/v1/admin/dues/semesters"))
                    .andExpect(jsonPath("$.semesters[*].id").value(contains("2027-1", "2026-2")))
                    .andExpect(jsonPath("$.semesters[0].monthlyAmount").value(20_000));
        }

        @Test
        void 상세는_이름_학번_순이고_연결이_없는_대상_회원은_전_월_UNPAID다() throws Exception {
            member("20240003", "나회원").save();
            member("20240002", "가회원").save();
            member("20240001", "가회원").save();
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());

            perform(get("/v1/admin/dues/semesters/2026-2/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.semester.id").value("2026-2"))
                    .andExpect(jsonPath("$.semester.totalMembers").value(3))
                    .andExpect(jsonPath("$.members[*].studentNumber")
                            .value(contains("20240001", "20240002", "20240003")))
                    .andExpect(jsonPath("$.members[0].track").value("BACKEND"))
                    .andExpect(jsonPath("$.members[0].slackId").value(nullValue()))
                    .andExpect(jsonPath("$.members[0].status").value("UNPAID"))
                    .andExpect(jsonPath("$.members[0].assessedAmount").value(60_000))
                    .andExpect(jsonPath("$.members[0].paidAmount").value(0))
                    .andExpect(jsonPath("$.members[0].unpaidAmount").value(60_000))
                    .andExpect(jsonPath("$.members[0].excessAmount").value(0))
                    .andExpect(jsonPath("$.members[0].months[*].month").value(contains(
                            "2026-09", "2026-10", "2026-11", "2026-12", "2027-01", "2027-02")))
                    .andExpect(jsonPath("$.members[0].months[*].status").value(everyItem(
                            org.hamcrest.Matchers.is("UNPAID"))))
                    .andExpect(jsonPath("$.members[0].months[0].exemptions", hasSize(0)))
                    .andExpect(jsonPath("$.members[0].months[0].note").value(nullValue()));
        }

        @Test
        void 납부_비대상_회원은_EXEMPT이고_금액은_null이며_전_월_NOT_APPLICABLE이다() throws Exception {
            member("20240001", "비대상").duesRequired(false).save();
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());

            perform(get("/v1/admin/dues/semesters/2026-2/members"))
                    .andExpect(jsonPath("$.members[0].status").value("EXEMPT"))
                    .andExpect(jsonPath("$.members[0].assessedAmount").value(nullValue()))
                    .andExpect(jsonPath("$.members[0].paidAmount").value(nullValue()))
                    .andExpect(jsonPath("$.members[0].unpaidAmount").value(nullValue()))
                    .andExpect(jsonPath("$.members[0].excessAmount").value(0))
                    .andExpect(jsonPath("$.members[0].months[*].status").value(everyItem(
                            org.hamcrest.Matchers.is("NOT_APPLICABLE"))))
                    .andExpect(jsonPath("$.semester.exemptMembers").value(1))
                    .andExpect(jsonPath("$.semester.needsReview").value(false));
        }

        @Test
        void 학기_ID_형식이_틀리면_400_없으면_404다() throws Exception {
            perform(get("/v1/admin/dues/semesters/2026-3/members"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("학기 형식이 올바르지 않습니다."));
            perform(get("/v1/admin/dues/semesters/abc/members")).andExpect(status().isBadRequest());
            perform(get("/v1/admin/dues/semesters/2026-2/members"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("학기 회비를 찾을 수 없습니다."));
        }
    }

    @Nested
    class 권한 {

        @Test
        void 관리자가_아니면_403이다() throws Exception {
            member("20240001", "일반").save();
            String memberToken = login("20240001");
            createSemester(2026, 2, 10_000).andExpect(status().isCreated());

            perform(get("/v1/admin/dues/semesters"), memberToken).andExpect(status().isForbidden());
            perform(get("/v1/admin/dues/semesters/creatable"), memberToken).andExpect(status().isForbidden());
            perform(get("/v1/admin/dues/semesters/2026-2/members"), memberToken).andExpect(status().isForbidden());
            perform(post("/v1/admin/dues/semesters"), memberToken,
                    "{\"year\":2027,\"term\":1,\"monthlyAmount\":10000}")
                    .andExpect(status().isForbidden());
        }

        @Test
        void 토큰이_없으면_401이다() throws Exception {
            mockMvc.perform(get("/v1/admin/dues/semesters")).andExpect(status().isUnauthorized());
        }
    }
}
