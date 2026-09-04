package com.bcsdlab.bcsdinternalapiv2.game;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.game.config.GameBuildProperties;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameBuildRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminGameBuildIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameBuildRepository gameBuildRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GameBuildProperties gameBuildProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private Game game;

    @BeforeEach
    void setUp() throws Exception {
        gameRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        TrackMaster gameTrack = trackMasterRepository.findByCode("GAME").orElseThrow();

        Member admin = memberRepository.save(Member.builder()
                .studentNumber("20261111")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트")
                .track(gameTrack)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("admin@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build());
        adminToken = login("20261111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);

        game = gameRepository.save(Game.builder()
                .slug("neon-drift").name("Neon Drift").oneLiner("한 줄 소개")
                .track(gameTrack).displayOrder(0).published(true).build());
    }

    @Test
    @DisplayName("빌드 메타를 등록하면 status=PENDING이고 buildFileUrl이 없다(ADR-023)")
    void 빌드_메타_등록은_PENDING_상태다() throws Exception {
        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.2.0\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value("1.2.0"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.buildFileUrl").doesNotExist());

        mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("AC-9.10 활성 빌드가 없으면 게임을 공개해도 500이 아니고 activeBuild가 null이다")
    void 빌드가_PENDING이면_공개_응답에_노출되지_않는다() throws Exception {
        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.2.0\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeBuild").doesNotExist());
    }

    @Test
    @DisplayName("빌드 메타를 삭제하면 목록에서 사라진다")
    void 빌드_메타_삭제() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.0.0\"}"))
                .andReturn().getResponse().getContentAsString();
        long buildId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/v1/admin/games/" + game.getId() + "/builds/" + buildId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 게임에 빌드를 등록하면 404다")
    void 존재하지_않는_게임에_빌드_등록은_404() throws Exception {
        mockMvc.perform(post("/v1/admin/games/999999/builds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"1.0.0\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("AC-9.15 PENDING 빌드에 업로드 토큰을 발급하면 status가 PROCESSING이 되고, 다시 발급하면 409다")
    void 업로드_토큰_발급은_상태를_PROCESSING으로_바꾸고_재발급은_409() throws Exception {
        long buildId = createBuild("1.0.0");

        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds/" + buildId + "/upload-token")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));

        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds/" + buildId + "/upload-token")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AC-9.16 웹훅 시크릿이 틀리면 401이고 빌드 상태는 바뀌지 않는다")
    void 웹훅_시크릿이_틀리면_401() throws Exception {
        long buildId = createBuild("1.0.0");
        issueUploadToken(buildId);

        mockMvc.perform(post("/v1/games/builds/" + buildId + "/webhook")
                        .header("X-Game-Build-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));
    }

    @Test
    @DisplayName("AC-9.17 웹훅으로 ACTIVE를 통지하면 빌드가 활성화되고 기존 ACTIVE 빌드는 ARCHIVED로 내려간다")
    void 웹훅_ACTIVE_통지는_기존_활성_빌드를_보관한다() throws Exception {
        long firstBuildId = createBuild("1.0.0");
        issueUploadToken(firstBuildId);
        activateBuild(firstBuildId, "https://bcsdlab.com/games/neon-drift/index.html");

        long secondBuildId = createBuild("1.1.0");
        issueUploadToken(secondBuildId);
        activateBuild(secondBuildId, "https://bcsdlab.com/games/neon-drift/index.html");

        mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[?(@.id == " + secondBuildId + ")].status").value("ACTIVE"))
                .andExpect(jsonPath("$[?(@.id == " + firstBuildId + ")].status").value("ARCHIVED"));

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(jsonPath("$.activeBuild.version").value("1.1.0"));
    }

    @Test
    @DisplayName("AC-9.18 웹훅으로 FAILED를 통지하면 failureReason이 저장된다")
    void 웹훅_FAILED_통지는_failureReason을_저장한다() throws Exception {
        long buildId = createBuild("1.0.0");
        issueUploadToken(buildId);

        mockMvc.perform(post("/v1/games/builds/" + buildId + "/webhook")
                        .header("X-Game-Build-Secret", gameBuildProperties.secret())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FAILED\",\"failureReason\":\"loader.js를 찾을 수 없습니다\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[0].status").value("FAILED"))
                .andExpect(jsonPath("$[0].failureReason").value("loader.js를 찾을 수 없습니다"));
    }

    private long createBuild(String version) throws Exception {
        String body = mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"%s\"}".formatted(version)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private void issueUploadToken(long buildId) throws Exception {
        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/builds/" + buildId + "/upload-token")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private void activateBuild(long buildId, String buildFileUrl) throws Exception {
        mockMvc.perform(post("/v1/games/builds/" + buildId + "/webhook")
                        .header("X-Game-Build-Secret", gameBuildProperties.secret())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE","canvasWidth":960,"canvasHeight":600,
                                 "storageBytes":104857600,"buildFileUrl":"%s"}
                                """.formatted(buildFileUrl)))
                .andExpect(status().isNoContent());
    }

    private String login(String studentNumber) throws Exception {
        String body = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentNumber\":\"%s\",\"password\":\"%s\",\"rememberMe\":false}"
                                .formatted(studentNumber, RAW_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }
}
