package com.bcsdlab.bcsdinternalapiv2.game;

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

class AdminGameScreenshotRatingMemberIntegrationTest extends IntegrationTestSupport {

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
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster gameTrack;
    private String adminToken;
    private Game game;

    @BeforeEach
    void setUp() throws Exception {
        gameRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        gameTrack = trackMasterRepository.findByCode("GAME").orElseThrow();

        Member admin = memberRepository.save(newMember("20251111", "admin@bcsd.club"));
        adminToken = login("20251111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);

        game = gameRepository.save(Game.builder()
                .slug("neon-drift").name("Neon Drift").oneLiner("한 줄 소개")
                .track(gameTrack).displayOrder(0).published(true).build());
    }

    @Test
    @DisplayName("AC-9.6 스크린샷을 전체 교체하면 순서대로 display_order가 부여된다")
    void 스크린샷_전체_교체() throws Exception {
        mockMvc.perform(put("/v1/admin/games/" + game.getId() + "/screenshots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrls\":[\"https://x/1.png\",\"https://x/2.png\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].displayOrder").value(0))
                .andExpect(jsonPath("$[1].displayOrder").value(1));

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(jsonPath("$.screenshots.length()").value(2))
                .andExpect(jsonPath("$.screenshots[0]").value("https://x/1.png"))
                .andExpect(jsonPath("$.thumbnailUrl").value("https://x/1.png"));
    }

    @Test
    @DisplayName("스크린샷을 빈 배열로 교체하면 썸네일도 사라진다 (활동 사진 INV-12와 같은 규약)")
    void 스크린샷을_비우면_썸네일도_사라진다() throws Exception {
        mockMvc.perform(put("/v1/admin/games/" + game.getId() + "/screenshots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrls\":[\"https://x/1.png\"]}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/v1/admin/games/" + game.getId() + "/screenshots")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageUrls\":[]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/games/neon-drift")).andExpect(jsonPath("$.thumbnailUrl").doesNotExist());
    }

    @Test
    @DisplayName("AC-9.7 contentDescriptors를 생략하면 7개 항목이 모두 false로 저장된다")
    void 내용정보_생략시_전부_false() throws Exception {
        mockMvc.perform(put("/v1/admin/games/" + game.getId() + "/rating")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":\"ALL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentDescriptors.length()").value(0));
    }

    @Test
    @DisplayName("등급정보에 정의되지 않은 내용정보 키를 보내면 400이다(INV-21)")
    void 정의되지_않은_내용정보는_400() throws Exception {
        mockMvc.perform(put("/v1/admin/games/" + game.getId() + "/rating")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":\"ALL\",\"contentDescriptors\":[\"foo\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AC-9.8 등급정보를 삭제하면 공개 응답의 rating이 null이 된다")
    void 등급정보_삭제하면_rating_null() throws Exception {
        mockMvc.perform(put("/v1/admin/games/" + game.getId() + "/rating")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":\"OVER_15\",\"contentDescriptors\":[\"violence\",\"language\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value("OVER_15"))
                .andExpect(jsonPath("$.contentDescriptors.length()").value(2));

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(jsonPath("$.rating.rating").value("OVER_15"));

        mockMvc.perform(delete("/v1/admin/games/" + game.getId() + "/rating")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(jsonPath("$.rating").doesNotExist());
    }

    @Test
    @DisplayName("AC-9.9 명부에 없는 memberId로 참여 멤버를 배정하면 404다")
    void 존재하지_않는_멤버_배정은_404() throws Exception {
        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[999999]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("참여 멤버 배정·순서 변경·해제가 공개 응답에 반영된다")
    void 참여_멤버_배정_순서_해제() throws Exception {
        Member m1 = memberRepository.save(newMember("20252222", "m1@bcsd.club"));
        Member m2 = memberRepository.save(newMember("20253333", "m2@bcsd.club"));

        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d,%d]}".formatted(m1.getId(), m2.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // 같은 부원을 다시 배정하면 409다.
        mockMvc.perform(post("/v1/admin/games/" + game.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d]}".formatted(m1.getId())))
                .andExpect(status().isConflict());

        String membersBody = mockMvc.perform(get("/v1/admin/games/" + game.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn().getResponse().getContentAsString();
        long firstAssignmentId = objectMapper.readTree(membersBody).get(0).get("id").asLong();
        long secondAssignmentId = objectMapper.readTree(membersBody).get(1).get("id").asLong();

        mockMvc.perform(patch("/v1/admin/games/" + game.getId() + "/members/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(secondAssignmentId, firstAssignmentId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(jsonPath("$.members[0].name").value(m2.getName()))
                .andExpect(jsonPath("$.members[1].name").value(m1.getName()));

        mockMvc.perform(delete("/v1/admin/games/" + game.getId() + "/members/" + m1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/games/neon-drift"))
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].name").value(m2.getName()));
    }

    private Member newMember(String studentNumber, String email) {
        return Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트" + studentNumber)
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
}
