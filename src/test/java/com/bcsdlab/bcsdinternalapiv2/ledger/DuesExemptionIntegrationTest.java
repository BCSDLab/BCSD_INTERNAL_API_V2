package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

class DuesExemptionIntegrationTest extends LedgerIntegrationTestSupport {

    private static final List<String> SEED = List.of("멘토", "회장", "부회장", "트랙장", "교육장", "팀장", "졸업", "군휴학",
            "질병휴학", "휴학", "활동 중지", "프로젝트 미참여", "부트캠프", "취업", "연락두절");
    private static final String DETAIL = "/v1/admin/dues/semesters/2026-2/members";
    private static final String PREVIEW = "/v1/admin/dues/semesters/2026-2/exemption-preview";

    private Member alice;
    private Member bob;
    private Member outsider;

    @BeforeEach
    void setUpRoster() throws Exception {
        alice = member("20240001", "가회원").save();
        bob = member("20240002", "나회원").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        outsider = member("20240003", "다명단밖").save();
    }

    // ---------- 사유 ----------

    @Test
    void 사유_목록은_시드_15개를_순서와_문구_그대로_준다() throws Exception {
        perform(get("/v1/admin/dues/exemption-reasons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reasons").value(contains(SEED.toArray())));
    }

    @Test
    void 새_사유는_면제를_저장할_때_시드_다음에_등록_순으로_추가된다() throws Exception {
        create(alice.getId(), "교환학생", "2026-09", "2026-09").andExpect(status().isCreated());
        create(bob.getId(), "인턴", "2026-10", null).andExpect(status().isCreated());
        create(bob.getId(), "휴학", "2026-11", null).andExpect(status().isCreated());

        List<String> expected = new ArrayList<>(SEED);
        expected.addAll(List.of("교환학생", "인턴"));
        perform(get("/v1/admin/dues/exemption-reasons"))
                .andExpect(jsonPath("$.reasons").value(contains(expected.toArray())));
    }

    @Test
    void 사유는_앞뒤_공백을_지우고_NFC로_맞춰_저장하고_중복_등록하지_않는다() throws Exception {
        String nfd = Normalizer.normalize("교환학생", Normalizer.Form.NFD);
        assertThat(nfd).isNotEqualTo("교환학생");

        create(alice.getId(), "  " + nfd + " ", "2026-09", null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reason").value("교환학생"));
        create(bob.getId(), "교환학생", "2026-09", null).andExpect(status().isCreated());
        create(bob.getId(), Normalizer.normalize("휴학", Normalizer.Form.NFD), "2026-10", null)
                .andExpect(jsonPath("$.reason").value("휴학"));

        assertThat(reasonCount()).isEqualTo(16);
        assertThat(jdbcTemplate.queryForList("SELECT reason FROM dues_exemption", String.class))
                .allSatisfy(reason -> assertThat(Normalizer.isNormalized(reason, Normalizer.Form.NFC)).isTrue());
    }

    @Test
    void 같은_새_사유가_동시에_들어와도_하나만_생기고_모두_저장된다() throws Exception {
        int threads = 6;
        CountDownLatch start = new CountDownLatch(1);
        List<CompletableFuture<Integer>> calls = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Long memberId = i % 2 == 0 ? alice.getId() : bob.getId();
            calls.add(CompletableFuture.supplyAsync(() -> {
                try {
                    start.await(10, TimeUnit.SECONDS);
                    return create(memberId, "동시사유", "2026-09", null).andReturn().getResponse().getStatus();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }));
        }
        start.countDown();
        for (CompletableFuture<Integer> call : calls) {
            assertThat(call.get(20, TimeUnit.SECONDS)).isEqualTo(201);
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM dues_exemption_reason WHERE name = '동시사유'", Long.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dues_exemption", Long.class)).isEqualTo(threads);
    }

    // ---------- 면제 저장·목록 ----------

    @Test
    void 면제를_추가하면_201과_응답을_주고_목록은_id_순이다() throws Exception {
        String body = create(alice.getId(), "휴학", "2026-11", "2027-01", "비고")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(alice.getId().intValue()))
                .andExpect(jsonPath("$.reason").value("휴학"))
                .andExpect(jsonPath("$.note").value("비고"))
                .andExpect(jsonPath("$.startMonth").value("2026-11"))
                .andExpect(jsonPath("$.endMonth").value("2027-01"))
                .andReturn().getResponse().getContentAsString();
        long first = objectMapper.readTree(body).get("id").asLong();
        create(bob.getId(), "멘토", "2026-09", null)
                .andExpect(jsonPath("$.endMonth").value(nullValue()));

        perform(get("/v1/admin/dues/exemptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exemptions", hasSize(2)))
                .andExpect(jsonPath("$.exemptions[0].id").value((int) first))
                .andExpect(jsonPath("$.exemptions[1].memberId").value(bob.getId().intValue()));
    }

    @Test
    void 면제를_수정하면_통째로_바뀐다() throws Exception {
        long id = createdId(alice.getId(), "휴학", "2026-11", null);

        update(id, bob.getId(), "군휴학", "2026-09", "2026-10", "수정")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id))
                .andExpect(jsonPath("$.memberId").value(bob.getId().intValue()))
                .andExpect(jsonPath("$.reason").value("군휴학"))
                .andExpect(jsonPath("$.note").value("수정"))
                .andExpect(jsonPath("$.startMonth").value("2026-09"))
                .andExpect(jsonPath("$.endMonth").value("2026-10"));
        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(60_000))
                .andExpect(jsonPath("$.members[1].assessedAmount").value(40_000));
    }

    @Test
    void 기간이_거꾸로면_INVALID_EXEMPTION_PERIOD_400이다() throws Exception {
        create(alice.getId(), "휴학", "2026-11", "2026-10")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("종료 월은 시작 월보다 빠를 수 없습니다."));
        long id = createdId(alice.getId(), "휴학", "2026-11", null);
        update(id, alice.getId(), "휴학", "2027-03", "2027-02", "").andExpect(status().isBadRequest());
        preview(alice.getId(), "휴학", "2026-11", "2026-10", null).andExpect(status().isBadRequest());

        // 시작 월과 종료 월이 같으면 한 달짜리 면제다.
        create(alice.getId(), "휴학", "2026-11", "2026-11").andExpect(status().isCreated());
    }

    @Test
    void 형식이나_범위가_틀린_값은_400이다() throws Exception {
        create(alice.getId(), "휴학", "2026-13", null).andExpect(status().isBadRequest());
        create(alice.getId(), "휴학", "2026-00", null).andExpect(status().isBadRequest());
        create(alice.getId(), "휴학", "2026-9", null).andExpect(status().isBadRequest());
        create(alice.getId(), "휴학", "2026-09-01", null).andExpect(status().isBadRequest());
        create(alice.getId(), "휴학", "2026-09", "2026/10").andExpect(status().isBadRequest());
        create(alice.getId(), "   ", "2026-09", null).andExpect(status().isBadRequest());
        create(alice.getId(), "가".repeat(51), "2026-09", null).andExpect(status().isBadRequest());
        create(alice.getId(), "휴학", "2026-09", null, "가".repeat(501)).andExpect(status().isBadRequest());
        perform(post("/v1/admin/dues/exemptions"), adminToken,
                "{\"memberId\":%d,\"reason\":\"휴학\",\"note\":\"\"}".formatted(alice.getId()))
                .andExpect(status().isBadRequest());
        perform(post("/v1/admin/dues/exemptions"), adminToken,
                "{\"reason\":\"휴학\",\"note\":\"\",\"startMonth\":\"2026-09\"}")
                .andExpect(status().isBadRequest());

        // 공백을 지운 뒤 50자면 통과한다.
        create(alice.getId(), " " + "가".repeat(50) + " ", "2026-09", null).andExpect(status().isCreated());
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM dues_exemption", Long.class)).isEqualTo(1);
    }

    @Test
    void 없는_회원이면_MEMBER_NOT_FOUND_없는_면제면_EXEMPTION_NOT_FOUND_404() throws Exception {
        create(999_999L, "휴학", "2026-09", null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
        update(999_999L, alice.getId(), "휴학", "2026-09", null, "")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("면제 기록을 찾을 수 없습니다."));
        long id = createdId(alice.getId(), "휴학", "2026-11", null);
        update(id, 999_999L, "휴학", "2026-09", null, "")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
        assertThat(reasonCount()).isEqualTo(15);
    }

    // ---------- 집계 반영 ----------

    @Test
    void 면제가_걸린_달은_EXEMPT이고_그_달에_걸린_면제를_담으며_다른_달은_빈_배열이다() throws Exception {
        long id = createdId(alice.getId(), "휴학", "2026-12", null);

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(30_000))
                .andExpect(jsonPath("$.members[0].months[*].status").value(contains(
                        "UNPAID", "UNPAID", "UNPAID", "EXEMPT", "EXEMPT", "EXEMPT")))
                .andExpect(jsonPath("$.members[0].months[0].exemptions", hasSize(0)))
                .andExpect(jsonPath("$.members[0].months[3].exemptions", hasSize(1)))
                .andExpect(jsonPath("$.members[0].months[3].exemptions[0].id").value((int) id))
                .andExpect(jsonPath("$.members[0].months[3].exemptions[0].reason").value("휴학"))
                .andExpect(jsonPath("$.members[0].months[3].exemptions[0].startMonth").value("2026-12"))
                .andExpect(jsonPath("$.members[0].months[3].exemptions[0].endMonth").value(nullValue()))
                .andExpect(jsonPath("$.members[1].months[3].exemptions", hasSize(0)))
                .andExpect(jsonPath("$.semester.totalAmount").value(90_000));
    }

    @Test
    void 겹친_면제의_결과는_하나일_때와_같고_겹친_달에는_전부를_start_month_id_순으로_담는다() throws Exception {
        // alice: 11월 하나, bob: 11월에 겹친 면제 둘(나중에 만든 면제가 시작 월이 더 이르다)
        createdId(alice.getId(), "휴학", "2026-11", "2026-11");
        long later = createdId(bob.getId(), "휴학", "2026-11", "2026-11");
        long earlierStart = createdId(bob.getId(), "멘토", "2026-10", "2026-11");
        long sameStart = createdId(bob.getId(), "군휴학", "2026-11", "2026-11");

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(50_000))
                .andExpect(jsonPath("$.members[0].status").value("UNPAID"))
                .andExpect(jsonPath("$.members[1].assessedAmount").value(40_000))
                .andExpect(jsonPath("$.members[1].months[2].status").value("EXEMPT"))
                .andExpect(jsonPath("$.members[1].months[2].exemptions[*].id").value(contains(
                        (int) earlierStart, (int) later, (int) sameStart)))
                .andExpect(jsonPath("$.members[1].months[1].exemptions[*].id").value(contains((int) earlierStart)))
                .andExpect(jsonPath("$.members[1].months[0].exemptions", hasSize(0)));
    }

    @Test
    void 같은_달을_덮는_면제를_여러_개_넣어도_하나일_때와_결과가_같다() throws Exception {
        createdId(alice.getId(), "휴학", "2026-11", "2026-12");
        createdId(bob.getId(), "휴학", "2026-11", "2026-12");
        createdId(bob.getId(), "휴학", "2026-11", "2026-12");
        createdId(bob.getId(), "멘토", "2026-12", "2026-12");

        String body = perform(get(DETAIL)).andReturn().getResponse().getContentAsString();
        var members = objectMapper.readTree(body).get("members");
        for (String field : List.of("status", "assessedAmount", "paidAmount", "unpaidAmount", "excessAmount")) {
            assertThat(members.get(1).get(field)).as(field).isEqualTo(members.get(0).get(field));
        }
        for (int i = 0; i < 6; i++) {
            assertThat(members.get(1).get("months").get(i).get("status"))
                    .isEqualTo(members.get(0).get("months").get(i).get("status"));
        }
    }

    @Test
    void 여러_학기에_걸친_면제는_각_학기_범위의_달에만_반영된다() throws Exception {
        createdId(alice.getId(), "휴학", "2026-12", "2027-04");
        clock.setToday(LocalDate.of(2027, 3, 2));
        createSemester(2027, 1, 10_000).andExpect(status().isCreated());

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].months[*].status").value(contains(
                        "UNPAID", "UNPAID", "UNPAID", "EXEMPT", "EXEMPT", "EXEMPT")))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(30_000));
        perform(get("/v1/admin/dues/semesters/2027-1/members"))
                .andExpect(jsonPath("$.members[0].months[*].month").value(contains(
                        "2027-03", "2027-04", "2027-05", "2027-06", "2027-07", "2027-08")))
                .andExpect(jsonPath("$.members[0].months[*].status").value(contains(
                        "EXEMPT", "EXEMPT", "UNPAID", "UNPAID", "UNPAID", "UNPAID")))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(40_000));
        // 2027-1 명단에는 나중에 가입한 회원도 들어간다: 40,000 + 60,000 + 60,000 / 2026-2: 30,000 + 60,000
        perform(get("/v1/admin/dues/semesters"))
                .andExpect(jsonPath("$.semesters[*].totalAmount").value(contains(160_000, 90_000)));
    }

    @Test
    void 전_기간_면제이고_연결이_없으면_EXEMPT이고_요약에서_면제_회원으로_센다() throws Exception {
        createdId(alice.getId(), "멘토", "2026-01", null);

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].status").value("EXEMPT"))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(0))
                .andExpect(jsonPath("$.members[0].months[*].status").value(everyItem(is("EXEMPT"))))
                .andExpect(jsonPath("$.semester.exemptMembers").value(1))
                .andExpect(jsonPath("$.semester.targetMembers").value(1));
    }

    @Test
    void 납부_비대상_회원은_면제가_있어도_비대상_모양이다() throws Exception {
        perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/v1/admin/dues/semesters/2026-2/roster/{memberId}", alice.getId()), adminToken,
                "{\"applicable\":false}").andExpect(status().isOk());
        createdId(alice.getId(), "휴학", "2026-11", null);

        perform(get(DETAIL))
                .andExpect(jsonPath("$.members[0].assessedAmount").value(nullValue()))
                .andExpect(jsonPath("$.members[0].months[*].status").value(everyItem(is("NOT_APPLICABLE"))))
                .andExpect(jsonPath("$.members[0].months[3].exemptions", hasSize(0)));
    }

    // ---------- 미리보기 ----------

    @Test
    void 미리보기는_before_after를_주고_DB에_아무것도_쓰지_않는다() throws Exception {
        createdId(alice.getId(), "휴학", "2026-09", "2026-09");
        long exemptionsBefore = exemptionCount();
        long reasonsBefore = reasonCount();

        preview(alice.getId(), "교환학생", "2026-12", null, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.before.memberId").value(alice.getId().intValue()))
                .andExpect(jsonPath("$.before.assessedAmount").value(50_000))
                .andExpect(jsonPath("$.after.assessedAmount").value(20_000))
                .andExpect(jsonPath("$.after.months[*].status").value(contains(
                        "EXEMPT", "UNPAID", "UNPAID", "EXEMPT", "EXEMPT", "EXEMPT")))
                .andExpect(jsonPath("$.after.months[3].exemptions[0].id").value(-1))
                .andExpect(jsonPath("$.after.months[3].exemptions[0].reason").value("교환학생"));

        assertThat(exemptionCount()).isEqualTo(exemptionsBefore);
        assertThat(reasonCount()).isEqualTo(reasonsBefore);
        perform(get("/v1/admin/dues/exemption-reasons"))
                .andExpect(jsonPath("$.reasons", hasSize(15)));
        perform(get(DETAIL)).andExpect(jsonPath("$.members[0].assessedAmount").value(50_000));
    }

    @Test
    void editingId가_있으면_그_면제를_요청_값으로_바꾼_결과를_after로_준다() throws Exception {
        long editing = createdId(alice.getId(), "휴학", "2026-09", "2027-02");
        createdId(alice.getId(), "멘토", "2026-09", "2026-09");

        preview(alice.getId(), "휴학", "2026-12", "2027-02", editing)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.before.assessedAmount").value(0))
                .andExpect(jsonPath("$.after.assessedAmount").value(20_000))
                .andExpect(jsonPath("$.after.months[*].status").value(contains(
                        "EXEMPT", "UNPAID", "UNPAID", "EXEMPT", "EXEMPT", "EXEMPT")))
                .andExpect(jsonPath("$.after.months[3].exemptions[0].id").value((int) editing));

        assertThat(jdbcTemplate.queryForObject("SELECT start_month::text FROM dues_exemption WHERE id = ?",
                String.class, editing)).isEqualTo("2026-09-01");
    }

    @Test
    void 미리보기_404_코드() throws Exception {
        preview(alice.getId(), "휴학", "2026-09", null, 999_999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("면제 기록을 찾을 수 없습니다."));
        preview(999_999L, "휴학", "2026-09", null, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원을 찾을 수 없습니다."));
        preview(outsider.getId(), "휴학", "2026-09", null, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("이 학기 회비 명단에 없는 회원입니다."));
        perform(post("/v1/admin/dues/semesters/2025-1/exemption-preview"), adminToken,
                body(alice.getId(), "휴학", "2026-09", null, "", null))
                .andExpect(status().isNotFound());
        perform(post("/v1/admin/dues/semesters/2026-9/exemption-preview"), adminToken,
                body(alice.getId(), "휴학", "2026-09", null, "", null))
                .andExpect(status().isBadRequest());
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        String memberToken = login("20240001");

        perform(get("/v1/admin/dues/exemptions"), memberToken).andExpect(status().isForbidden());
        perform(get("/v1/admin/dues/exemption-reasons"), memberToken).andExpect(status().isForbidden());
        perform(post("/v1/admin/dues/exemptions"), memberToken, body(alice.getId(), "휴학", "2026-09", null, "", null))
                .andExpect(status().isForbidden());
        perform(put("/v1/admin/dues/exemptions/{id}", 1), memberToken,
                body(alice.getId(), "휴학", "2026-09", null, "", null))
                .andExpect(status().isForbidden());
        perform(post(PREVIEW), memberToken, body(alice.getId(), "휴학", "2026-09", null, "", null))
                .andExpect(status().isForbidden());
        assertThat(exemptionCount()).isZero();
    }

    // ---------- helpers ----------

    private ResultActions create(Long memberId, String reason, String start, String end) throws Exception {
        return create(memberId, reason, start, end, "");
    }

    private ResultActions create(Long memberId, String reason, String start, String end, String note)
            throws Exception {
        return perform(post("/v1/admin/dues/exemptions"), adminToken, body(memberId, reason, start, end, note, null));
    }

    private long createdId(Long memberId, String reason, String start, String end) throws Exception {
        String response = create(memberId, reason, start, end).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private ResultActions update(long id, Long memberId, String reason, String start, String end, String note)
            throws Exception {
        return perform(put("/v1/admin/dues/exemptions/{id}", id), adminToken,
                body(memberId, reason, start, end, note, null));
    }

    private ResultActions preview(Long memberId, String reason, String start, String end, Long editingId)
            throws Exception {
        return perform(post(PREVIEW), adminToken, body(memberId, reason, start, end, "", editingId));
    }

    private String body(Long memberId, String reason, String start, String end, String note, Long editingId)
            throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("memberId", memberId);
        body.put("reason", reason);
        body.put("note", note);
        body.put("startMonth", start);
        body.put("endMonth", end);
        body.put("editingId", editingId);
        return objectMapper.writeValueAsString(body);
    }

    private long exemptionCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM dues_exemption", Long.class);
    }

    private long reasonCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM dues_exemption_reason", Long.class);
    }
}
