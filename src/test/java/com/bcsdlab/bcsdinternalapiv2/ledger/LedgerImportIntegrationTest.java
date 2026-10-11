package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/** 합성 fixture로 가져오기 미리보기·반영을 확인한다. 실제 거래내역은 쓰지 않는다. */
class LedgerImportIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String PREVIEW = "/v1/admin/ledger/imports/preview";
    private static final String COMMIT = "/v1/admin/ledger/imports";
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    // ---------- 미리보기 ----------

    @Test
    void 미리보기는_거래를_장부_순서로_주고_저장하지_않는다() throws Exception {
        byte[] file = sameSecondFile();

        preview(file, "거래내역_2609.xlsx", XLSX)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("거래내역_2609.xlsx"))
                .andExpect(jsonPath("$.transactions", hasSize(4)))
                .andExpect(jsonPath("$.transactions[*].counterparty").value(contains("기초", "회원1", "가게", "회원3")))
                .andExpect(jsonPath("$.transactions[1].occurredAt").value("2026-09-01T10:00:00"))
                .andExpect(jsonPath("$.transactions[1].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.transactions[1].amount").value(10_000))
                .andExpect(jsonPath("$.transactions[1].bankBalance").value(110_000))
                .andExpect(jsonPath("$.transactions[1].rowKey").value(org.hamcrest.Matchers.matchesPattern("[0-9a-f]{64}")))
                .andExpect(jsonPath("$.transactions[1].suggestedCategory").value("ETC"))
                .andExpect(jsonPath("$.transactions[1].suggestedMemberId").value(nullValue()));

        assertThat(entryCount()).isZero();
    }

    @Test
    void 최근_학기_명단에_이름이_한_명만_같으면_그_회원과_DUES를_추천한다() throws Exception {
        Member unique = member("20240001", "회원1").save();
        member("20240002", "동명").save();
        member("20240003", "동명").save();
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        byte[] file = XlsxFixture.bankOriginal()
                .deposit("2026.09.01 10:00:00", 10_000, "회원1", 10_000)
                .deposit("2026.09.01 10:00:01", 10_000, "동명", 20_000)
                .withdrawal("2026.09.01 10:00:02", 5_000, "회원1", 15_000)
                .build();

        preview(file, "a.xlsx", XLSX)
                .andExpect(jsonPath("$.transactions[0].suggestedMemberId").value(unique.getId().intValue()))
                .andExpect(jsonPath("$.transactions[0].suggestedCategory").value("DUES"))
                .andExpect(jsonPath("$.transactions[1].suggestedMemberId").value(nullValue()))
                .andExpect(jsonPath("$.transactions[1].suggestedCategory").value("ETC"))
                .andExpect(jsonPath("$.transactions[2].suggestedCategory").value("DUES"));
    }

    @Test
    void 이미_장부에_있는_거래는_미리보기에서_빠진다() throws Exception {
        JsonNode preview = previewJson(sameSecondFile());
        commit(commitBody(preview, 0, 1)).andExpect(status().isOk());

        preview(sameSecondFile(), "a.xlsx", XLSX)
                .andExpect(jsonPath("$.transactions[*].counterparty").value(contains("가게", "회원3")));
    }

    @ParameterizedTest(name = "파트 Content-Type = {0}")
    @NullSource
    @ValueSource(strings = {XLSX, "application/octet-stream", ""})
    void 파트_Content_Type과_상관없이_같은_결과다(String contentType) throws Exception {
        preview(sameSecondFile(), "a.xlsx", contentType)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(4)));
    }

    @Test
    void 확장자가_xlsx가_아니거나_xlsx가_아니면_400이다() throws Exception {
        preview(sameSecondFile(), "a.xls", XLSX)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("확장자")));
        preview("텍스트".getBytes(), "a.xlsx", XLSX).andExpect(status().isBadRequest());
        preview(new byte[0], "a.xlsx", XLSX).andExpect(status().isBadRequest());
    }

    @Test
    void file_파트가_없거나_multipart가_아니면_400이다() throws Exception {
        perform(multipart(PREVIEW).file(new MockMultipartFile("other", "a.xlsx", XLSX, sameSecondFile())))
                .andExpect(status().isBadRequest());
        perform(post(PREVIEW), adminToken, "{}").andExpect(status().isBadRequest());
    }

    // ---------- 반영 ----------

    @Test
    void 반영하면_은행_잔액을_장부_잔액으로_같은_초_순서대로_넣는다() throws Exception {
        JsonNode preview = previewJson(sameSecondFile());

        commit(commitBody(preview))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(4))
                .andExpect(jsonPath("$.skippedDuplicateCount").value(0));

        perform(get("/v1/admin/ledger/entries"))
                .andExpect(jsonPath("$.entries[*].counterparty").value(contains("회원3", "가게", "회원1", "기초")))
                .andExpect(jsonPath("$.entries[*].balance").value(contains(112_000, 107_000, 110_000, 100_000)))
                .andExpect(jsonPath("$.entries[0].source").value("거래내역.xlsx"))
                .andExpect(jsonPath("$.entries[0].category").value("ETC"))
                .andExpect(jsonPath("$.entries[0].linkStatus").value("NONE"))
                .andExpect(jsonPath("$.entries[0].description").value("가져온 거래내역"));
        assertThat(jdbcTemplate.queryForList(
                "SELECT occurred_seq FROM ledger_entry ORDER BY occurred_at, occurred_seq", Short.class))
                .containsExactly((short) 0, (short) 0, (short) 1, (short) 2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ledger_entry WHERE bank_balance IS NULL OR row_key IS NULL", Long.class))
                .isZero();
    }

    @Test
    void 같은_파일을_두_번_반영하면_두_번째는_전부_건너뛴다() throws Exception {
        JsonNode preview = previewJson(sameSecondFile());
        commit(commitBody(preview)).andExpect(jsonPath("$.createdCount").value(4));

        commit(commitBody(preview))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdCount").value(0))
                .andExpect(jsonPath("$.skippedDuplicateCount").value(4));
        assertThat(entryCount()).isEqualTo(4);
    }

    @Test
    void DUES로_반영하면_PENDING이고_비고가_없으면_최근_학기_회비로_설명한다() throws Exception {
        createSemester(2026, 2, 10_000).andExpect(status().isCreated());
        ObjectNode body = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) body.get("transactions").get(1)).put("category", "DUES");
        ((ObjectNode) body.get("transactions").get(2)).put("note", "행사 비용");

        commit(body).andExpect(status().isOk());

        perform(get("/v1/admin/ledger/entries"))
                .andExpect(jsonPath("$.entries[2].category").value("DUES"))
                .andExpect(jsonPath("$.entries[2].linkStatus").value("PENDING"))
                .andExpect(jsonPath("$.entries[2].description").value("2026년 2학기 회비"))
                .andExpect(jsonPath("$.entries[1].description").value("행사 비용"))
                .andExpect(jsonPath("$.entries[1].note").value("행사 비용"));
    }

    @Test
    void rowKey를_다시_계산해_다르면_400이고_아무것도_넣지_않는다() throws Exception {
        ObjectNode body = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) body.get("transactions").get(3)).put("amount", 5_001);

        commit(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("거래 정보가 미리보기 결과와 다릅니다. 파일을 다시 미리보기 하세요."));
        assertThat(entryCount()).isZero();

        ObjectNode counterparty = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) counterparty.get("transactions").get(0)).put("counterparty", "다른상대");
        commit(counterparty).andExpect(status().isBadRequest());
    }

    @Test
    void 요청_안에_같은_거래가_두_번이면_400이고_아무것도_넣지_않는다() throws Exception {
        ObjectNode body = commitBody(previewJson(sameSecondFile()));
        ((ArrayNode) body.get("transactions")).add(body.get("transactions").get(2).deepCopy());

        commit(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("같은 거래가 요청에 두 번 있습니다. 파일을 다시 미리보기 하세요."));
        assertThat(entryCount()).isZero();

        // 중복을 뺀 같은 요청은 그대로 반영된다.
        commit(commitBody(previewJson(sameSecondFile())))
                .andExpect(jsonPath("$.createdCount").value(4))
                .andExpect(jsonPath("$.skippedDuplicateCount").value(0));
    }

    @Test
    void bankBalance가_없으면_400이다() throws Exception {
        ObjectNode body = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) body.get("transactions").get(0)).remove("bankBalance");

        commit(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("은행 잔액(bankBalance)이 없습니다."));
        assertThat(entryCount()).isZero();
    }

    @Test
    void 분류는_DUES와_ETC만_받는다() throws Exception {
        for (String category : List.of("EVENT", "OPERATION", "DUES_REFUND")) {
            ObjectNode body = commitBody(previewJson(sameSecondFile()));
            ((ObjectNode) body.get("transactions").get(0)).put("category", category);
            commit(body).andExpect(status().isBadRequest());
        }
        assertThat(entryCount()).isZero();
    }

    @Test
    void 거래가_없거나_5000건을_넘으면_400이다() throws Exception {
        ObjectNode empty = objectMapper.createObjectNode().put("fileName", "a.xlsx");
        empty.putArray("transactions");
        commit(empty).andExpect(status().isBadRequest());

        ObjectNode one = commitBody(previewJson(sameSecondFile()));
        ArrayNode many = objectMapper.createArrayNode();
        for (int i = 0; i < 5_001; i++) {
            many.add(one.get("transactions").get(0));
        }
        one.set("transactions", many);
        commit(one).andExpect(status().isBadRequest());
    }

    @Test
    void 요청_검증_실패는_400이다() throws Exception {
        String[][] cases = {
                {"rowKey", "XYZ"},
                {"occurredAt", "2026.09.01 10:00:00"},
                {"note", "가".repeat(501)},
        };
        for (String[] invalid : cases) {
            ObjectNode body = commitBody(previewJson(sameSecondFile()));
            ((ObjectNode) body.get("transactions").get(0)).put(invalid[0], invalid[1]);
            commit(body).andExpect(status().isBadRequest());
        }
        ObjectNode zero = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) zero.get("transactions").get(0)).put("amount", 0);
        commit(zero).andExpect(status().isBadRequest());
    }

    // ---------- 반영 + 증빙 ----------

    @Test
    void 증빙을_붙여_반영하면_새로_넣은_기록에_붙는다() throws Exception {
        Long evidence = confirmedEvidence();
        ObjectNode body = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) body.get("transactions").get(2)).putArray("evidenceIds").add(evidence);

        commit(body).andExpect(status().isOk());

        perform(get("/v1/admin/ledger/entries"))
                .andExpect(jsonPath("$.entries[1].counterparty").value("가게"))
                .andExpect(jsonPath("$.entries[1].evidences[0].id").value(evidence.intValue()));
    }

    @Test
    void 증빙_규칙을_어기면_전체를_롤백한다() throws Exception {
        Long confirmed = confirmedEvidence();
        Long pending = pendingEvidence();

        assertCommitWithEvidence(List.of(999_999L), 404);
        assertCommitWithEvidence(List.of(pending), 400);
        assertCommitWithEvidence(List.of(1L, 2L, 3L, 4L, 5L, 6L), 400);

        ObjectNode duplicated = commitBody(previewJson(sameSecondFile()));
        ((ObjectNode) duplicated.get("transactions").get(0)).putArray("evidenceIds").add(confirmed);
        ((ObjectNode) duplicated.get("transactions").get(1)).putArray("evidenceIds").add(confirmed);
        commit(duplicated)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("같은 증빙이 요청에 두 번 있습니다."));

        // 이미 다른 기록에 붙은 증빙
        Long entryId = deposit(1_000).getId();
        perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .patch("/v1/admin/ledger/entries/{id}", entryId), adminToken,
                "{\"counterparty\":\"상대\",\"category\":\"ETC\",\"description\":\"내용\",\"note\":\"\","
                        + "\"evidenceIds\":[%d]}".formatted(confirmed))
                .andExpect(status().isOk());
        assertCommitWithEvidence(List.of(confirmed), 400);

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ledger_entry WHERE row_key IS NOT NULL",
                Long.class)).isZero();
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        member("20249999", "일반").save();
        String memberToken = login("20249999");

        mockMvc.perform(multipart(PREVIEW).file(new MockMultipartFile("file", "a.xlsx", XLSX, sameSecondFile()))
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
        perform(post(COMMIT), memberToken, commitBody(previewJson(sameSecondFile())).toString())
                .andExpect(status().isForbidden());
        assertThat(entryCount()).isZero();
    }

    // ---------- helpers ----------

    /** 같은 초 거래 3건이 파일에 역순으로 들어 있는 합성 파일(fixture 4). */
    private static byte[] sameSecondFile() {
        return XlsxFixture.bankOriginal()
                .deposit("2026.09.01 09:59:59", 100_000, "기초", 100_000)
                .deposit("2026.09.01 10:00:00", 5_000, "회원3", 112_000)
                .withdrawal("2026.09.01 10:00:00", 3_000, "가게", 107_000)
                .deposit("2026.09.01 10:00:00", 10_000, "회원1", 110_000)
                .build();
    }

    private ResultActions preview(byte[] file, String fileName, String contentType) throws Exception {
        return perform(multipart(PREVIEW).file(new MockMultipartFile("file", fileName, contentType, file)));
    }

    private JsonNode previewJson(byte[] file) throws Exception {
        String body = preview(file, "거래내역.xlsx", XLSX).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    /** 미리보기 거래를 반영 요청으로 바꾼다. indexes가 있으면 그 거래만 담는다. */
    private ObjectNode commitBody(JsonNode preview, int... indexes) {
        ObjectNode body = objectMapper.createObjectNode().put("fileName", preview.get("fileName").asText());
        ArrayNode transactions = body.putArray("transactions");
        JsonNode source = preview.get("transactions");
        int[] picks = indexes.length == 0 ? java.util.stream.IntStream.range(0, source.size()).toArray() : indexes;
        for (int i : picks) {
            JsonNode t = source.get(i);
            ObjectNode item = transactions.addObject()
                    .put("rowKey", t.get("rowKey").asText())
                    .put("occurredAt", t.get("occurredAt").asText())
                    .put("type", t.get("type").asText())
                    .put("counterparty", t.get("counterparty").asText())
                    .put("amount", t.get("amount").asLong())
                    .put("bankBalance", t.get("bankBalance").asLong())
                    .put("category", t.get("suggestedCategory").asText())
                    .put("note", "");
            item.putArray("evidenceIds");
        }
        return body;
    }

    private ResultActions commit(JsonNode body) throws Exception {
        return perform(post(COMMIT), adminToken, body.toString());
    }

    private void assertCommitWithEvidence(List<Long> evidenceIds, int expectedStatus) throws Exception {
        ObjectNode body = commitBody(previewJson(sameSecondFile()));
        ArrayNode ids = ((ObjectNode) body.get("transactions").get(0)).putArray("evidenceIds");
        evidenceIds.forEach(ids::add);
        commit(body).andExpect(status().is(expectedStatus));
    }

    private Long pendingEvidence() throws Exception {
        String body = perform(post("/v1/admin/ledger/evidences/presigned-url"), adminToken,
                "{\"fileName\":\"r.png\",\"contentType\":\"image/png\",\"size\":100}")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("evidenceId").asLong();
    }

    private Long confirmedEvidence() throws Exception {
        Long id = pendingEvidence();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().contentLength(100L).contentType("image/png").build());
        perform(post("/v1/admin/ledger/evidences/{id}/complete", id)).andExpect(status().isOk());
        return id;
    }

    private long entryCount() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM ledger_entry", Long.class);
    }
}
