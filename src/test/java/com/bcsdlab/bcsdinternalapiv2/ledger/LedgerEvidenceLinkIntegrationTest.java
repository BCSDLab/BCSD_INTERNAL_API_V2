package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.ResultActions;

class LedgerEvidenceLinkIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String ENTRIES = "/v1/admin/ledger/entries";
    private static final String DRIVE = "https://drive.google.com/file/d/";
    private static final String HOST_ERROR = "구글 드라이브 링크만 첨부할 수 있습니다.";

    // ---------- 붙이기·교체·삭제 ----------

    @Test
    void 링크를_붙이면_붙인_순서로_응답과_목록에_보이고_공백을_지운다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link(" " + DRIVE + "a/view ", " 영수증 "), link("https://docs.google.com/document/d/b", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences", hasSize(2)))
                .andExpect(jsonPath("$.evidences[0].url").value(DRIVE + "a/view"))
                .andExpect(jsonPath("$.evidences[0].label").value("영수증"))
                .andExpect(jsonPath("$.evidences[0].id").isNumber())
                .andExpect(jsonPath("$.evidences[1].url").value("https://docs.google.com/document/d/b"))
                .andExpect(jsonPath("$.evidences[1].label").value(nullValue()));

        perform(get(ENTRIES))
                .andExpect(jsonPath("$.entries[0].evidences[*].url")
                        .value(contains(DRIVE + "a/view", "https://docs.google.com/document/d/b")));
    }

    @Test
    void 교체하면_남은_링크는_행을_두고_이름만_고치고_빠진_링크는_지우고_새_링크는_뒤에_붙인다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        update(entry.getId(), link(DRIVE + "a", "처음"), link(DRIVE + "b", null)).andExpect(status().isOk());
        Long keptId = linkId(entry.getId(), DRIVE + "a");

        update(entry.getId(), link(DRIVE + "c", null), link(DRIVE + "a", "바뀐 이름"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences[*].url").value(contains(DRIVE + "a", DRIVE + "c")))
                .andExpect(jsonPath("$.evidences[0].id").value(keptId.intValue()))
                .andExpect(jsonPath("$.evidences[0].label").value("바뀐 이름"));
        assertThat(linkCount(entry.getId())).isEqualTo(2);
    }

    @Test
    void 빈_목록이면_모든_링크를_뗀다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        update(entry.getId(), link(DRIVE + "a", null)).andExpect(status().isOk());

        update(entry.getId()).andExpect(status().isOk()).andExpect(jsonPath("$.evidences", hasSize(0)));
        assertThat(linkCount(entry.getId())).isZero();
    }

    @Test
    void 분류를_바꿔도_링크는_유지된다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        update(entry.getId(), link(DRIVE + "a", null)).andExpect(status().isOk());

        patchEntry(entry.getId(), "EVENT", List.of(link(DRIVE + "a", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("EVENT"))
                .andExpect(jsonPath("$.evidences", hasSize(1)));
    }

    @Test
    void 다른_기록에는_같은_링크를_붙일_수_있다() throws Exception {
        LedgerEntry first = deposit(10_000);
        LedgerEntry second = deposit(20_000);

        update(first.getId(), link(DRIVE + "a", null)).andExpect(status().isOk());
        update(second.getId(), link(DRIVE + "a", null)).andExpect(status().isOk());
    }

    // ---------- 검증 ----------

    @ParameterizedTest
    @ValueSource(strings = {
            "http://drive.google.com/file/d/a",
            "https://example.com/a",
            "https://drive.google.com.evil.com/a",
            "https://evil.com/drive.google.com",
            "https://drive.google.com@evil.com/a",
            "https://user@drive.google.com/a",
            "https://drive.google.com:8443/a",
            "javascript:alert(1)",
            "ftp://drive.google.com/a",
            "drive.google.com/file/d/a",
            "https://drive.google.com/a b",
            "https:///a",
    })
    void 구글_드라이브_https_링크가_아니면_400이고_아무것도_바뀌지_않는다(String url) throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link(url, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(HOST_ERROR));
        assertThat(linkCount(entry.getId())).isZero();
    }

    @Test
    void 호스트와_스킴은_대소문자를_가리지_않고_보낸_그대로_저장한다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link("HTTPS://Drive.Google.com/file/d/A", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences[0].url").value("HTTPS://Drive.Google.com/file/d/A"));
    }

    @Test
    void 빈_링크와_null_항목은_입력_안내다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link("   ", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙 링크를 입력하세요."));
        patchBody(entry.getId(), body("ETC", "[null]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙 링크를 입력하세요."));
        patchBody(entry.getId(), body("ETC", "[{\"label\":\"이름만\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙 링크를 입력하세요."));
    }

    @Test
    void 목록을_빼면_400이다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        patchBody(entry.getId(), "{\"counterparty\":\"상대\",\"category\":\"ETC\",\"description\":\"내용\",\"note\":\"\","
                + "\"evidenceLinks\":null}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙 목록을 입력하세요."));
    }

    @Test
    void 링크는_공백을_지운_뒤_2048자까지다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        String max = DRIVE + "a".repeat(2048 - DRIVE.length());

        update(entry.getId(), link(" " + max + " ", null)).andExpect(status().isOk());
        update(entry.getId(), link(max + "a", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙 링크는 2,048자 이하여야 합니다."));
    }

    @Test
    void 이름은_공백을_지운_뒤_50자까지다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link(DRIVE + "a", " " + "가".repeat(50) + " ")).andExpect(status().isOk());
        update(entry.getId(), link(DRIVE + "a", "가".repeat(51)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("링크 이름은 50자 이하로 입력하세요."));
    }

    @Test
    void 다섯_개까지_붙이고_여섯_개면_400이고_기존_링크는_그대로다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        List<Map<String, Object>> five = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            five.add(link(DRIVE + i, null));
        }
        patchEntry(entry.getId(), "ETC", five).andExpect(status().isOk());

        List<Map<String, Object>> six = new ArrayList<>(five);
        six.add(link(DRIVE + 6, null));
        patchEntry(entry.getId(), "ETC", six)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙은 최대 5개까지 첨부할 수 있습니다."));
        assertThat(linkCount(entry.getId())).isEqualTo(5);
    }

    @Test
    void 같은_기록에_같은_링크가_두_번이면_400이고_공백만_다른_것도_같다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link(DRIVE + "a", "하나"), link(" " + DRIVE + "a", "둘"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("같은 증빙 링크가 두 번 있습니다."));
        assertThat(linkCount(entry.getId())).isZero();
    }

    @Test
    void 쿼리나_슬래시가_다르면_다른_링크다() throws Exception {
        LedgerEntry entry = deposit(10_000);

        update(entry.getId(), link(DRIVE + "a", null), link(DRIVE + "a/", null), link(DRIVE + "a?usp=sharing", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences", hasSize(3)));
    }

    // ---------- 예전 S3 증빙 ----------

    @Test
    void S3_증빙_API는_없어졌다() throws Exception {
        perform(post("/v1/admin/ledger/evidences/presigned-url"), adminToken,
                "{\"fileName\":\"r.png\",\"contentType\":\"image/png\",\"size\":100}")
                .andExpect(status().isNotFound());
        perform(post("/v1/admin/ledger/evidences/{id}/complete", 1)).andExpect(status().isNotFound());
    }

    @Test
    void V27은_기존_S3_증빙의_공개_URL과_파일명을_링크로_옮긴다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        LedgerEntry other = deposit(20_000);
        long first = imageAsset("ledger_evidence/1.png", "https://cdn.example.com/1.png", "  영수증.png ");
        long second = imageAsset("ledger_evidence/2.pdf", "https://cdn.example.com/2.pdf", "가".repeat(60) + ".pdf");
        long third = imageAsset("ledger_evidence/3.png", "https://cdn.example.com/3.png", " ");
        jdbcTemplate.update("INSERT INTO ledger_entry_evidence (image_asset_id, entry_id) VALUES (?, ?), (?, ?), (?, ?)",
                first, entry.getId(), second, entry.getId(), third, other.getId());

        jdbcTemplate.update(copyStatementOfV27());
        // 다시 돌려도 (entry_id, url) 유니크라 늘지 않는다.
        jdbcTemplate.update(copyStatementOfV27());

        perform(get(ENTRIES))
                .andExpect(jsonPath("$.entries[0].evidences", hasSize(1)))
                .andExpect(jsonPath("$.entries[0].evidences[0].url").value("https://cdn.example.com/3.png"))
                .andExpect(jsonPath("$.entries[0].evidences[0].label").value(nullValue()))
                .andExpect(jsonPath("$.entries[1].evidences[*].url")
                        .value(contains("https://cdn.example.com/1.png", "https://cdn.example.com/2.pdf")))
                .andExpect(jsonPath("$.entries[1].evidences[0].label").value("영수증.png"))
                .andExpect(jsonPath("$.entries[1].evidences[1].label").value("가".repeat(50)));
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM ledger_entry_evidence", Long.class))
                .isEqualTo(3);
    }

    @Test
    void 옮긴_S3_링크는_다시_보내면_남고_새_링크만_드라이브_규칙을_따른다() throws Exception {
        LedgerEntry entry = deposit(10_000);
        LedgerEntry other = deposit(20_000);
        jdbcTemplate.update("INSERT INTO ledger_entry_evidence_link (entry_id, url, label) VALUES (?, ?, ?)",
                entry.getId(), "https://cdn.example.com/1.png", "영수증.png");

        update(entry.getId(), link("https://cdn.example.com/1.png", "이름 바꿈"), link(DRIVE + "a", null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences[*].url").value(contains("https://cdn.example.com/1.png", DRIVE + "a")))
                .andExpect(jsonPath("$.evidences[0].label").value("이름 바꿈"));

        // 다른 기록에 새로 붙이는 것은 안 된다.
        update(other.getId(), link("https://cdn.example.com/1.png", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(HOST_ERROR));

        // 한 번 뺀 뒤에는 다시 붙일 수 없다.
        update(entry.getId(), link(DRIVE + "a", null)).andExpect(status().isOk());
        update(entry.getId(), link(DRIVE + "a", null), link("https://cdn.example.com/1.png", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(HOST_ERROR));
        assertThat(linkCount(entry.getId())).isEqualTo(1);
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        member("20240009", "일반").save();
        String memberToken = login("20240009");
        LedgerEntry entry = deposit(10_000);

        perform(patch(ENTRIES + "/{id}", entry.getId()), memberToken, body("ETC", "[{\"url\":\"" + DRIVE + "a\"}]"))
                .andExpect(status().isForbidden());
        assertThat(linkCount(entry.getId())).isZero();
    }

    @SafeVarargs
    private ResultActions update(Long id, Map<String, Object>... links) throws Exception {
        return patchEntry(id, "ETC", List.of(links));
    }

    private ResultActions patchEntry(Long id, String category, List<Map<String, Object>> links) throws Exception {
        return patchBody(id, objectMapper.writeValueAsString(Map.of(
                "counterparty", "상대", "category", category, "description", "내용", "note", "",
                "evidenceLinks", links)));
    }

    private ResultActions patchBody(Long id, String body) throws Exception {
        return perform(patch(ENTRIES + "/{id}", id), adminToken, body);
    }

    private static String body(String category, String linksJson) {
        return "{\"counterparty\":\"상대\",\"category\":\"%s\",\"description\":\"내용\",\"note\":\"\",\"evidenceLinks\":%s}"
                .formatted(category, linksJson);
    }

    private static Map<String, Object> link(String url, String label) {
        Map<String, Object> link = new HashMap<>();
        link.put("url", url);
        link.put("label", label);
        return link;
    }

    private long linkCount(Long entryId) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM ledger_entry_evidence_link WHERE entry_id = ?",
                Long.class, entryId);
    }

    private Long linkId(Long entryId, String url) {
        return jdbcTemplate.queryForObject("SELECT id FROM ledger_entry_evidence_link WHERE entry_id = ? AND url = ?",
                Long.class, entryId, url);
    }

    private long imageAsset(String s3Key, String url, String originalName) {
        return jdbcTemplate.queryForObject("INSERT INTO image_asset (s3_key, url, original_name, content_type, byte_size, "
                        + "purpose, is_confirmed) VALUES (?, ?, ?, 'image/png', 100, 'LEDGER_EVIDENCE', TRUE) RETURNING id",
                Long.class, s3Key, url, originalName);
    }

    /** 마이그레이션 파일의 복사 문장을 그대로 실행한다(테스트 DB에는 V27이 빈 테이블일 때 이미 적용돼 있다). */
    private static String copyStatementOfV27() throws Exception {
        String sql = new ClassPathResource("db/migration/V27__create_ledger_entry_evidence_link.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        String statement = sql.substring(sql.indexOf("INSERT INTO ledger_entry_evidence_link"));
        return statement.substring(0, statement.lastIndexOf(';'));
    }
}
