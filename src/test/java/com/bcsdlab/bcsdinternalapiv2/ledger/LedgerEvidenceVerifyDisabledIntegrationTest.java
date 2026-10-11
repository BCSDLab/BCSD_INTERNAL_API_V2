package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.config.LedgerEvidenceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

/**
 * LEDGER_EVIDENCE_VERIFY_UPLOAD=false(운영 IAM에 HeadObject 권한이 없을 때)의 동작. S3를 부르지 않고 확정하며,
 * 발급 단계의 검증은 그대로다. 설정이 달라 Spring 컨텍스트가 하나 더 뜬다.
 */
@TestPropertySource(properties = "app.ledger.evidence.verify-upload=false")
class LedgerEvidenceVerifyDisabledIntegrationTest extends LedgerIntegrationTestSupport {

    @Autowired
    private LedgerEvidenceProperties properties;

    @Test
    void 업로드_확인을_끄면_HeadObject_없이_확정한다() throws Exception {
        assertThat(properties.verifyUpload()).isFalse();
        String body = perform(post("/v1/admin/ledger/evidences/presigned-url"), adminToken,
                "{\"fileName\":\"a.pdf\",\"contentType\":\"application/pdf\",\"size\":100}")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("evidenceId").asLong();

        perform(post("/v1/admin/ledger/evidences/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("a.pdf"));

        verify(s3Client, never()).headObject(any(HeadObjectRequest.class));
        assertThat(jdbcTemplate.queryForObject("SELECT is_confirmed FROM image_asset WHERE id = ?", Boolean.class,
                id)).isTrue();
    }

    @Test
    void 업로드_확인을_꺼도_발급_단계_검증은_그대로다() throws Exception {
        perform(post("/v1/admin/ledger/evidences/presigned-url"), adminToken,
                "{\"fileName\":\"a.svg\",\"contentType\":\"image/svg+xml\",\"size\":100}")
                .andExpect(status().isBadRequest());
        perform(post("/v1/admin/ledger/evidences/presigned-url"), adminToken,
                "{\"fileName\":\"a.pdf\",\"contentType\":\"image/png\",\"size\":100}")
                .andExpect(status().isBadRequest());
        perform(post("/v1/admin/ledger/evidences/presigned-url"), adminToken,
                "{\"fileName\":\"a.png\",\"contentType\":\"image/png\",\"size\":10485761}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void 업로드_확인을_꺼도_일반_미디어_ID는_404다() throws Exception {
        perform(post("/v1/admin/ledger/evidences/{id}/complete", 999_999L)).andExpect(status().isNotFound());
    }
}
