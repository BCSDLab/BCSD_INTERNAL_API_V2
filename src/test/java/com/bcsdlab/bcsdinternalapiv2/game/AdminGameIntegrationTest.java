package com.bcsdlab.bcsdinternalapiv2.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameMember;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameRating;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameRatingLevel;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameScreenshot;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameBuildRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRatingRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameScreenshotRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminGameIntegrationTest extends IntegrationTestSupport {

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
    private GameScreenshotRepository gameScreenshotRepository;

    @Autowired
    private GameRatingRepository gameRatingRepository;

    @Autowired
    private GameBuildRepository gameBuildRepository;

    @Autowired
    private GameMemberRepository gameMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster gameTrack;
    private String adminToken;
    private String memberToken;

    @BeforeEach
    void setUp() throws Exception {
        gameRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        gameTrack = trackMasterRepository.findByCode("GAME").orElseThrow();

        Member admin = memberRepository.save(newMember("20241111", "admin@bcsd.club"));
        adminToken = login("20241111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);

        memberRepository.save(newMember("20242222", "member@bcsd.club"));
        memberToken = login("20242222");
    }

    @Test
    @DisplayName("토큰 없이 호출하면 401이다")
    void 토큰_없이_호출하면_401() throws Exception {
        mockMvc.perform(post("/v1/admin/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameCreateBody("Neon Drift")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("MEMBER 권한으로 호출하면 403이다")
    void member_권한으로_호출하면_403() throws Exception {
        mockMvc.perform(post("/v1/admin/games")
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameCreateBody("Neon Drift")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AC-9.1 게임명에서 slug를 자동 생성한다")
    void slug를_자동_생성한다() throws Exception {
        mockMvc.perform(post("/v1/admin/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameCreateBody("Neon Drift")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("neon-drift"))
                .andExpect(jsonPath("$.name").value("Neon Drift"))
                .andExpect(jsonPath("$.isPublished").value(false));
    }

    @Test
    @DisplayName("AC-9.2 중복 slug는 409다")
    void 중복_slug는_409() throws Exception {
        gameRepository.save(game("neon-drift", 0, true));

        mockMvc.perform(post("/v1/admin/games")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameCreateBody("Neon Drift")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("AC-9.4 순서 변경이 공개 목록에 즉시 반영된다")
    void 순서_변경이_공개_목록에_즉시_반영된다() throws Exception {
        Game a = gameRepository.save(game("neon-drift", 0, true));
        Game b = gameRepository.save(game("pixel-farm", 1, true));

        mockMvc.perform(patch("/v1/admin/games/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(b.getId(), a.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/games"))
                .andExpect(jsonPath("$[0].slug").value("pixel-farm"))
                .andExpect(jsonPath("$[1].slug").value("neon-drift"));
    }

    @Test
    @DisplayName("AC-9.5 id 집합이 다르면 400이고 아무것도 변경되지 않는다")
    void id_집합이_다르면_아무것도_바뀌지_않는다() throws Exception {
        Game a = gameRepository.save(game("neon-drift", 0, true));
        gameRepository.save(game("pixel-farm", 1, true));

        mockMvc.perform(patch("/v1/admin/games/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d]}".formatted(a.getId())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/v1/games"))
                .andExpect(jsonPath("$[0].slug").value("neon-drift"))
                .andExpect(jsonPath("$[1].slug").value("pixel-farm"));
    }

    @Test
    @DisplayName("AC-9.12 상세 설명의 script 태그는 저장 시 제거된다")
    void 상세_설명의_script_태그는_제거된다() throws Exception {
        Game game = gameRepository.save(game("neon-drift", 0, false));

        String body = mockMvc.perform(put("/v1/admin/games/" + game.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Neon Drift\",\"oneLiner\":\"one\",\"teamLabel\":\"1팀\","
                                + "\"description\":\"<p>안내</p><script>alert(1)</script>\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String description = objectMapper.readTree(body).get("description").asText();
        assertThat(description).doesNotContainIgnoringCase("<script").contains("안내");
    }

    @Test
    @DisplayName("AC-9.13 상세 설명의 외부 호스트 이미지는 저장 시 제거된다")
    void 상세_설명의_외부_이미지는_제거된다() throws Exception {
        Game game = gameRepository.save(game("neon-drift", 0, false));

        String body = mockMvc.perform(put("/v1/admin/games/" + game.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Neon Drift\",\"oneLiner\":\"one\",\"teamLabel\":\"1팀\","
                                + "\"description\":\"<img src=\\\"http://evil.com/x.png\\\">\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String description = objectMapper.readTree(body).get("description").asText();
        assertThat(description).doesNotContain("evil.com");
    }

    @Test
    @DisplayName("AC-9.11 게임 삭제(soft delete) 후 공개·관리자 조회 모두 404다")
    void 게임_삭제_후_공개_조회는_404다() throws Exception {
        Game game = gameRepository.save(game("neon-drift", 0, true));

        mockMvc.perform(delete("/v1/admin/games/" + game.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/games/neon-drift")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v1/admin/games/" + game.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    /**
     * game_screenshot/rating/build/member의 on delete cascade(ADR-006)는 게임 행이
     * 실제로 DELETE될 때만 발동한다 — soft delete(deleted_at 마킹)는 UPDATE라 발동하지
     * 않는다. 이 테스트는 그 DB 제약 자체를 검증한다(관리자 API 경로가 아니라 하드 삭제 시).
     */
    @Test
    @DisplayName("INV-7 game 행을 실제로 삭제하면 하위 데이터가 cascade로 함께 삭제된다")
    void 게임_행을_하드_삭제하면_하위_데이터가_cascade로_삭제된다() {
        Game game = gameRepository.save(game("neon-drift", 0, true));
        gameScreenshotRepository.save(
                GameScreenshot.builder().game(game).imageUrl("https://x/1.png").displayOrder(0).build());
        gameRatingRepository.save(GameRating.builder().game(game).rating(GameRatingLevel.ALL).build());
        gameBuildRepository.save(GameBuild.builder().game(game).version("1.0.0").status(GameBuildStatus.ACTIVE)
                .uploadedAt(Instant.now()).build());
        Member participant = memberRepository.save(newMember("20243333", "p@bcsd.club"));
        gameMemberRepository.save(GameMember.builder().game(game).member(participant).displayOrder(0).build());

        gameRepository.deleteById(game.getId());

        assertThat(gameScreenshotRepository.findAllByGame_IdOrderByDisplayOrderAsc(game.getId())).isEmpty();
        assertThat(gameRatingRepository.findByGame_Id(game.getId())).isEmpty();
        assertThat(gameBuildRepository.findAllByGame_IdOrderByUploadedAtDesc(game.getId())).isEmpty();
        assertThat(gameMemberRepository.findAllByGame_IdOrderByDisplayOrderAsc(game.getId())).isEmpty();
    }

    private Member newMember(String studentNumber, String email) {
        return Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트")
                .track(gameTrack)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email(email)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private String login(String studentNumber) throws Exception {
        String body = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentNumber\":\"%s\",\"password\":\"%s\",\"rememberMe\":false}"
                                .formatted(studentNumber, RAW_PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("accessToken").asText();
    }

    private String gameCreateBody(String name) {
        return "{\"name\":\"%s\",\"oneLiner\":\"one liner\",\"trackId\":%d,\"teamLabel\":\"1팀\"}"
                .formatted(name, gameTrack.getId());
    }

    private Game game(String slug, int order, boolean published) {
        return Game.builder()
                .slug(slug)
                .name(slug)
                .oneLiner("한 줄 소개")
                .track(gameTrack)
                .displayOrder(order)
                .published(published)
                .build();
    }
}
