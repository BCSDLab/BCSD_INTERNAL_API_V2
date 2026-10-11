package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.LedgerEntry;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.EvidenceWriter;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** 업로드 확인(verify-upload)이 켜진 기본 설정. 꺼진 경우는 {@link LedgerEvidenceVerifyDisabledIntegrationTest}. */
class LedgerEvidenceIntegrationTest extends LedgerIntegrationTestSupport {

    private static final String EVIDENCES = "/v1/admin/ledger/evidences";
    private static final long MAX = 10L * 1024 * 1024;

    @Autowired
    private ImageAssetRepository imageAssetRepository;

    @Autowired
    private EvidenceWriter evidenceWriter;

    // ---------- 발급 ----------

    @Test
    void 발급하면_장부_증빙_행을_만들고_형식과_크기를_서명한_URL을_준다() throws Exception {
        Long adminId = memberRepository.findByStudentNumber("2020000000").orElseThrow().getId();

        Long evidenceId = issue("영수증.PDF", "application/pdf", 2048);

        ImageAsset asset = imageAssetRepository.findById(evidenceId).orElseThrow();
        assertThat(asset.getPurpose()).isEqualTo(ImagePurpose.LEDGER_EVIDENCE);
        assertThat(asset.getS3Key()).startsWith("ledger_evidence/").endsWith(".pdf");
        assertThat(asset.getOriginalName()).isEqualTo("영수증.PDF");
        assertThat(asset.getByteSize()).isEqualTo(2048);
        assertThat(asset.getUploadedBy()).isEqualTo(adminId);
        assertThat(asset.isConfirmed()).isFalse();
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("application/pdf");
        assertThat(captor.getValue().putObjectRequest().contentLength()).isEqualTo(2048L);
    }

    @Test
    void 발급_응답은_evidenceId와_uploadUrl이다() throws Exception {
        presign("a.png", "image/png", 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidenceId").isNumber())
                .andExpect(jsonPath("$.uploadUrl").value(startsWith("https://")))
                .andExpect(jsonPath("$.publicUrl").doesNotExist());
    }

    @ParameterizedTest(name = "{0} {1} 허용")
    @CsvSource({
            "a.png, image/png",
            "a.jpg, image/jpeg",
            "a.JPEG, image/jpeg",
            "a.webp, image/webp",
            "a.pdf, application/pdf",
    })
    void 허용_형식(String fileName, String contentType) throws Exception {
        presign(fileName, contentType, MAX).andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0} {1} {2} 거부")
    @CsvSource({
            "a.svg, image/svg+xml, 100",
            "a.heic, image/heic, 100",
            "a.gif, image/gif, 100",
            "a.png, image/svg+xml, 100",
            "a.exe, application/pdf, 100",
            "a.pdf, image/png, 100",
            "a.jpg, image/png, 100",
            "a.png, image/jpeg, 100",
            "a.png, image/png, 10485761",
            "a.png, image/png, 0",
            "png, image/png, 100",
    })
    void 허용하지_않는_형식_짝이_틀린_형식_크기는_400(String fileName, String contentType, long size)
            throws Exception {
        presign(fileName, contentType, size).andExpect(status().isBadRequest());
        assertThat(imageAssetRepository.count()).isZero();
    }

    @Test
    void 확장자와_형식이_맞지_않으면_안내_문구를_준다() throws Exception {
        presign("영수증.pdf", "image/png", 100)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("파일 확장자와 형식이 맞지 않습니다."));
        presign("a.png", "image/png", MAX + 1)
                .andExpect(jsonPath("$.message").value("증빙은 10MB 이하여야 합니다."));
    }

    // ---------- 완료 (업로드 확인 켜짐) ----------

    @Test
    void 업로드된_객체가_신청과_같으면_확정하고_증빙을_돌려준다() throws Exception {
        Long id = issue("receipt.png", "image/png", 1234);
        head(1234, "image/png");

        complete(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.intValue()))
                .andExpect(jsonPath("$.name").value("receipt.png"))
                .andExpect(jsonPath("$.url").value(startsWith("https://")))
                .andExpect(jsonPath("$.width").value(nullValue()))
                .andExpect(jsonPath("$.height").value(nullValue()));

        assertThat(imageAssetRepository.findById(id).orElseThrow().isConfirmed()).isTrue();
        ArgumentCaptor<HeadObjectRequest> captor = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3Client).headObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(imageAssetRepository.findById(id).orElseThrow().getS3Key());
    }

    @Test
    void 크기가_다르면_400이고_확정하지_않는다() throws Exception {
        Long id = issue("receipt.png", "image/png", 1234);
        head(1235, "image/png");

        complete(id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙 파일이 업로드되지 않았거나 신청한 형식·크기와 다릅니다."));
        assertUnconfirmed(id);
    }

    @Test
    void 형식이_다르면_400이고_매개변수와_대소문자는_무시한다() throws Exception {
        Long id = issue("receipt.png", "image/png", 1234);
        head(1234, "image/svg+xml");
        complete(id).andExpect(status().isBadRequest());
        assertUnconfirmed(id);

        head(1234, "Image/PNG; charset=binary");
        complete(id).andExpect(status().isOk());
    }

    @Test
    void 객체가_없으면_400이다() throws Exception {
        Long id = issue("receipt.png", "image/png", 1234);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().statusCode(404).build());
        complete(id).andExpect(status().isBadRequest());

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());
        complete(id).andExpect(status().isBadRequest());
        assertUnconfirmed(id);
    }

    @Test
    void 권한이_없으면_원인을_알_수_있는_502이고_확정하지_않는다() throws Exception {
        Long id = issue("receipt.png", "image/png", 1234);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(403).build());

        complete(id)
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message")
                        .value("서버에 증빙 업로드를 확인할 권한이 없어 완료하지 못했습니다. 관리자에게 문의하세요."));
        assertUnconfirmed(id);
    }

    @Test
    void S3_호출이_실패하면_502이고_확정하지_않는다() throws Exception {
        Long id = issue("receipt.png", "image/png", 1234);
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(500).build());
        complete(id).andExpect(status().isBadGateway());

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(SdkClientException.create("timeout"));
        complete(id)
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("증빙 업로드를 확인하지 못했습니다. 잠시 뒤 다시 시도하세요."));
        assertUnconfirmed(id);
    }

    @Test
    void 이미_확정된_증빙은_다시_확인하지_않고_그대로_돌려준다() throws Exception {
        Long id = confirmedEvidence("receipt.png");

        complete(id).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id.intValue()));

        verify(s3Client, org.mockito.Mockito.times(1)).headObject(any(HeadObjectRequest.class));
    }

    @Test
    void 일반_미디어_ID나_없는_ID는_404다() throws Exception {
        ImageAsset hero = saveAsset(ImagePurpose.TRACK_HERO, false);

        complete(hero.getId())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("증빙을 찾을 수 없습니다."));
        complete(999_999L).andExpect(status().isNotFound());
        assertUnconfirmed(hero.getId());
        verify(s3Client, never()).headObject(any(HeadObjectRequest.class));
    }

    // ---------- 장부 수정의 evidenceIds ----------

    @Test
    void 증빙을_붙이면_id_순으로_응답에_담기고_목록에도_보인다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        Long first = confirmedEvidence("a.png");
        Long second = confirmedEvidence("b.pdf");

        updateEvidences(entry.getId(), second, first)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences[*].id").value(contains(first.intValue(), second.intValue())))
                .andExpect(jsonPath("$.evidences[0].name").value("a.png"))
                .andExpect(jsonPath("$.evidences[1].width").value(nullValue()));

        perform(get("/v1/admin/ledger/entries"))
                .andExpect(jsonPath("$.entries[0].evidences", hasSize(2)));
    }

    @Test
    void 교체하면_빠진_증빙의_연결만_지우고_파일_행은_남는다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        Long kept = confirmedEvidence("a.png");
        Long removed = confirmedEvidence("b.png");
        Long added = confirmedEvidence("c.png");
        updateEvidences(entry.getId(), kept, removed).andExpect(status().isOk());

        updateEvidences(entry.getId(), kept, added)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences[*].id").value(contains(kept.intValue(), added.intValue())));

        assertThat(attachedEntryOf(removed)).isNull();
        assertThat(imageAssetRepository.findById(removed)).isPresent();

        // 떼어 낸 증빙은 다른 기록에 붙일 수 있다.
        LedgerEntry other = withdrawal(5_000);
        updateEvidences(other.getId(), removed).andExpect(status().isOk());
    }

    @Test
    void 빈_목록이면_모든_증빙을_뗀다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        Long evidence = confirmedEvidence("a.png");
        updateEvidences(entry.getId(), evidence).andExpect(status().isOk());

        updateEvidences(entry.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences", hasSize(0)));
        assertThat(attachedEntryOf(evidence)).isNull();
    }

    @Test
    void 다섯_개까지_붙이고_여섯_개면_400이다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        Long[] ids = new Long[6];
        for (int i = 0; i < 6; i++) {
            ids[i] = confirmedEvidence("e" + i + ".png");
        }

        updateEvidences(entry.getId(), ids[0], ids[1], ids[2], ids[3], ids[4])
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evidences", hasSize(5)));
        updateEvidences(entry.getId(), ids)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("증빙은 최대 5개까지 첨부할 수 있습니다."));
    }

    @Test
    void 다른_기록에_붙은_증빙은_400이고_아무것도_바뀌지_않는다() throws Exception {
        LedgerEntry owner = withdrawal(10_000);
        LedgerEntry other = withdrawal(5_000);
        Long shared = confirmedEvidence("a.png");
        Long free = confirmedEvidence("b.png");
        updateEvidences(owner.getId(), shared).andExpect(status().isOk());

        updateEvidences(other.getId(), free, shared)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("업로드가 끝나지 않았거나 다른 기록에 붙은 증빙입니다."));

        assertThat(attachedEntryOf(shared)).isEqualTo(owner.getId());
        assertThat(attachedEntryOf(free)).isNull();
    }

    @Test
    void 업로드가_확정되지_않은_증빙은_400이다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        Long pending = issue("a.png", "image/png", 100);

        updateEvidences(entry.getId(), pending).andExpect(status().isBadRequest());
        assertThat(attachedEntryOf(pending)).isNull();
    }

    @Test
    void 일반_미디어_ID를_붙이면_404다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        ImageAsset hero = saveAsset(ImagePurpose.TRACK_HERO, true);

        updateEvidences(entry.getId(), hero.getId()).andExpect(status().isNotFound());
    }

    @Test
    void 분류를_바꿔도_증빙은_유지된다() throws Exception {
        LedgerEntry entry = withdrawal(10_000);
        Long evidence = confirmedEvidence("a.png");

        perform(patch("/v1/admin/ledger/entries/{id}", entry.getId()), adminToken,
                "{\"counterparty\":\"상대\",\"category\":\"EVENT\",\"description\":\"내용\",\"note\":\"\","
                        + "\"evidenceIds\":[%d]}".formatted(evidence))
                .andExpect(jsonPath("$.category").value("EVENT"))
                .andExpect(jsonPath("$.evidences", hasSize(1)));
    }

    @Test
    void 저장_단계에서_다른_기록과_겹치면_400으로_바꾸고_기존_연결은_그대로다() throws Exception {
        LedgerEntry owner = withdrawal(10_000);
        LedgerEntry other = withdrawal(5_000);
        Long evidence = confirmedEvidence("a.png");
        updateEvidences(owner.getId(), evidence).andExpect(status().isOk());

        // 사전 확인을 건너뛰고 붙여, 확인과 저장 사이에 다른 요청이 같은 증빙을 붙인 경우를 만든다.
        assertThatThrownBy(() -> evidenceWriter.attach(evidence, other.getId(), Instant.now()))
                .isInstanceOf(LedgerException.class)
                .extracting(e -> ((LedgerException) e).getExceptionType())
                .isEqualTo(LedgerExceptionType.EVIDENCE_NOT_AVAILABLE);
        assertThat(attachedEntryOf(evidence)).isEqualTo(owner.getId());
    }

    // ---------- 권한 ----------

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        Member member = member("20240001", "일반").save();
        String memberToken = login("20240001");
        Long id = issue("a.png", "image/png", 100);

        perform(post(EVIDENCES + "/presigned-url"), memberToken,
                "{\"fileName\":\"a.png\",\"contentType\":\"image/png\",\"size\":100}")
                .andExpect(status().isForbidden());
        perform(post(EVIDENCES + "/{id}/complete", id), memberToken).andExpect(status().isForbidden());
        assertThat(member.getId()).isNotNull();
        assertUnconfirmed(id);
    }

    // ---------- helpers ----------

    private ResultActions presign(String fileName, String contentType, long size) throws Exception {
        return perform(post(EVIDENCES + "/presigned-url"), adminToken,
                "{\"fileName\":\"%s\",\"contentType\":\"%s\",\"size\":%d}".formatted(fileName, contentType, size));
    }

    private Long issue(String fileName, String contentType, long size) throws Exception {
        String body = presign(fileName, contentType, size).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("evidenceId").asLong();
    }

    private Long confirmedEvidence(String fileName) throws Exception {
        String contentType = fileName.endsWith(".pdf") ? "application/pdf" : "image/png";
        Long id = issue(fileName, contentType, 100);
        head(100, contentType);
        complete(id).andExpect(status().isOk());
        return id;
    }

    private void head(long contentLength, String contentType) {
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder()
                .contentLength(contentLength).contentType(contentType).build());
    }

    private ResultActions complete(Long id) throws Exception {
        return perform(post(EVIDENCES + "/{id}/complete", id));
    }

    private ResultActions updateEvidences(Long entryId, Long... evidenceIds) throws Exception {
        return perform(patch("/v1/admin/ledger/entries/{id}", entryId), adminToken,
                objectMapper.writeValueAsString(java.util.Map.of("counterparty", "상대", "category", "ETC",
                        "description", "내용", "note", "", "evidenceIds", List.of(evidenceIds))));
    }

    private ImageAsset saveAsset(ImagePurpose purpose, boolean confirmed) {
        ImageAsset asset = imageAssetRepository.save(ImageAsset.builder()
                .s3Key(purpose.name().toLowerCase() + "/" + java.util.UUID.randomUUID() + ".png")
                .url("https://image.example.com/x.png")
                .originalName("x.png")
                .contentType("image/png")
                .byteSize(100)
                .purpose(purpose)
                .build());
        if (confirmed) {
            jdbcTemplate.update("UPDATE image_asset SET is_confirmed = TRUE WHERE id = ?", asset.getId());
        }
        return asset;
    }

    private void assertUnconfirmed(Long id) {
        assertThat(jdbcTemplate.queryForObject("SELECT is_confirmed FROM image_asset WHERE id = ?", Boolean.class,
                id)).isFalse();
    }

    private Long attachedEntryOf(Long evidenceId) {
        List<Long> rows = jdbcTemplate.queryForList(
                "SELECT entry_id FROM ledger_entry_evidence WHERE image_asset_id = ?", Long.class, evidenceId);
        return rows.isEmpty() ? null : rows.getFirst();
    }
}
