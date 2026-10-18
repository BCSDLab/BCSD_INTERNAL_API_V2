package com.bcsdlab.bcsdinternalapiv2.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.member.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class AdminImageIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private ImageAssetRepository imageAssetRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private S3Presigner s3Presigner;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.update("delete from image_asset");
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        TrackMaster backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        Member admin = memberRepository.save(Member.builder()
                .studentNumber("20231111")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("관리자")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("admin@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build());
        String body = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentNumber\":\"20231111\",\"password\":\"%s\",\"rememberMe\":false}"
                                .formatted(RAW_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("accessToken").asText();
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);

        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        when(presigned.url()).thenReturn(URI.create("https://s3.example.com/upload?X-Amz-Signature=fake").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presigned);
    }

    @Test
    @DisplayName("AC-4.1 5MB를 초과하면 400이다")
    void 용량_초과는_400() throws Exception {
        mockMvc.perform(post("/v1/admin/images/presigned-url")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hero.png\",\"contentType\":\"image/png\","
                                + "\"byteSize\":6000000,\"purpose\":\"TRACK_HERO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AC-4.2 허용 외 확장자는 400이다")
    void 허용외_확장자는_400() throws Exception {
        mockMvc.perform(post("/v1/admin/images/presigned-url")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"malware.exe\",\"contentType\":\"application/octet-stream\","
                                + "\"byteSize\":1000,\"purpose\":\"TRACK_HERO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AC-4.3, AC-4.4 complete 전에는 라이브러리에 없고, complete 후에는 조회된다")
    void complete_전후_라이브러리_노출() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/images/presigned-url")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hero.png\",\"contentType\":\"image/png\","
                                + "\"byteSize\":1000,\"purpose\":\"TRACK_HERO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").exists())
                .andExpect(jsonPath("$.publicUrl").exists())
                .andReturn().getResponse().getContentAsString();
        long imageId = objectMapper.readTree(body).get("imageId").asLong();

        mockMvc.perform(get("/v1/admin/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("purpose", "TRACK_HERO"))
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(post("/v1/admin/images/" + imageId + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());

        mockMvc.perform(get("/v1/admin/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("purpose", "TRACK_HERO"))
                .andExpect(jsonPath("$.content.length()").value(1));

        assertThat(imageAssetRepository.findById(imageId).orElseThrow().isConfirmed()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이미지의 complete는 404다")
    void 존재하지_않는_이미지_complete는_404() throws Exception {
        mockMvc.perform(post("/v1/admin/images/999999/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ---------- 장부 증빙 유형(LEDGER_EVIDENCE)은 이 API에서 거부 ----------

    @Test
    @DisplayName("장부 증빙 유형으로 업로드 URL을 발급하면 400이고 행을 만들지 않는다")
    void 장부_증빙_유형_발급은_400() throws Exception {
        mockMvc.perform(post("/v1/admin/images/presigned-url")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"receipt.png\",\"contentType\":\"image/png\","
                                + "\"byteSize\":1000,\"purpose\":\"LEDGER_EVIDENCE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이 API에서 쓸 수 없는 이미지 유형입니다."));
        assertThat(imageAssetRepository.count()).isZero();
    }

    @Test
    @DisplayName("장부 증빙을 이 API로 complete하면 404이고 확정되지 않는다")
    void 장부_증빙_complete는_404() throws Exception {
        ImageAsset evidence = imageAssetRepository.save(ImageAsset.builder()
                .s3Key("ledger_evidence/test.png")
                .url("https://image.example.com/ledger_evidence/test.png")
                .originalName("test.png")
                .contentType("image/png")
                .byteSize(1000)
                .purpose(ImagePurpose.LEDGER_EVIDENCE)
                .build());

        mockMvc.perform(post("/v1/admin/images/" + evidence.getId() + "/complete")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        assertThat(imageAssetRepository.findById(evidence.getId()).orElseThrow().isConfirmed()).isFalse();
    }

    @Test
    @DisplayName("장부 증빙 유형으로 라이브러리를 조회하면 400이다")
    void 장부_증빙_라이브러리_조회는_400() throws Exception {
        mockMvc.perform(get("/v1/admin/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("purpose", "LEDGER_EVIDENCE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기존 유형의 업로드 URL은 예전처럼 크기를 서명하지 않는다")
    void 기존_유형_presign은_크기를_서명하지_않는다() throws Exception {
        mockMvc.perform(post("/v1/admin/images/presigned-url")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hero.png\",\"contentType\":\"image/png\","
                                + "\"byteSize\":1000,\"purpose\":\"TRACK_HERO\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());
        assertThat(captor.getValue().putObjectRequest().contentType()).isEqualTo("image/png");
        assertThat(captor.getValue().putObjectRequest().contentLength()).isNull();
        assertThat(imageAssetRepository.findAll()).singleElement()
                .satisfies(image -> assertThat(image.getUploadedBy()).isNull());
    }

    @Test
    @DisplayName("라이브러리 조회에 purpose가 없으면 500이 아니라 400이다")
    void purpose_없는_라이브러리_조회는_400() throws Exception {
        mockMvc.perform(get("/v1/admin/images")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("필수 요청 파라미터 'purpose'가 없습니다."));
    }

    @Test
    @DisplayName("라이브러리 조회에 없는 purpose 값을 주면 500이 아니라 400이다")
    void 없는_purpose_값은_400() throws Exception {
        mockMvc.perform(get("/v1/admin/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("purpose", "NOT_A_PURPOSE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("라이브러리 조회에 없는 필드로 정렬하면 500이 아니라 400이다")
    void 없는_필드_정렬은_400() throws Exception {
        mockMvc.perform(get("/v1/admin/images")
                        .header("Authorization", "Bearer " + adminToken)
                        .queryParam("purpose", "TRACK_HERO")
                        .queryParam("sort", "noSuchField,desc"))
                .andExpect(status().isBadRequest());
    }
}
