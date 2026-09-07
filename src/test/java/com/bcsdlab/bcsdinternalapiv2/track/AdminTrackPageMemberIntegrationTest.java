package com.bcsdlab.bcsdinternalapiv2.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminTrackPageMemberIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TrackPageMemberRepository trackPageMemberRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster backend;
    private TrackPage trackPage;
    private String adminToken;
    private Member alice;
    private Member bob;

    @BeforeEach
    void setUp() throws Exception {
        trackPageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        trackPage = trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());

        Member admin = memberRepository.save(newMember("20231111", "admin@bcsd.club"));
        adminToken = login("20231111");
        memberRepository.updateRole(admin.getId(), MemberRole.ADMIN);

        alice = memberRepository.save(newMember("20240001", "alice@bcsd.club"));
        bob = memberRepository.save(newMember("20240002", "bob@bcsd.club"));
    }

    @Test
    @DisplayName("부원을 배정하면 목록에 이름·등급·프로필 사진과 함께 나타난다")
    void 부원_배정과_목록_조회() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d,%d]}".formatted(alice.getId(), bob.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(alice.getName()))
                .andExpect(jsonPath("$[0].memberType").value("REGULAR"))
                .andExpect(jsonPath("$[0].isVisible").value(true));

        mockMvc.perform(get("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("INV-8 같은 부원을 두 번 배정하면 409다")
    void 같은_부원_중복_배정은_409() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d]}".formatted(alice.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d]}".formatted(alice.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("배정을 해제하면 목록에서 사라진다")
    void 배정_해제() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d]}".formatted(alice.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/v1/admin/track-pages/" + trackPage.getId() + "/members/" + alice.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("숨김으로 전환하면 배정은 유지되고 isVisible만 바뀐다")
    void 숨김_전환() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d]}".formatted(alice.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/v1/admin/track-pages/" + trackPage.getId() + "/members/" + alice.getId()
                        + "/visibility")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isVisible\":false}"))
                .andExpect(status().isNoContent());

        assertThat(trackPageMemberRepository
                .findByTrackPage_IdAndMember_Id(trackPage.getId(), alice.getId()).orElseThrow().isVisible())
                .isFalse();
    }

    @Test
    @DisplayName("순서를 바꾸면 목록 응답 순서가 바뀐다")
    void 순서_변경() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[%d,%d]}".formatted(alice.getId(), bob.getId())))
                .andReturn().getResponse().getContentAsString();
        long aliceAssignmentId = objectMapper.readTree(body).get(0).get("id").asLong();
        long bobAssignmentId = objectMapper.readTree(body).get(1).get("id").asLong();

        mockMvc.perform(patch("/v1/admin/track-pages/" + trackPage.getId() + "/members/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(bobAssignmentId, aliceAssignmentId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/track-pages/" + trackPage.getId() + "/members")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$[0].name").value(bob.getName()))
                .andExpect(jsonPath("$[1].name").value(alice.getName()));
    }

    private Member newMember(String studentNumber, String email) {
        return Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("테스트" + studentNumber)
                .track(backend)
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
