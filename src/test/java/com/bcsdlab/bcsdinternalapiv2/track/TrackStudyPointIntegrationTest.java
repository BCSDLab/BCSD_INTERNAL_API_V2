package com.bcsdlab.bcsdinternalapiv2.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackStudyPointRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class TrackStudyPointIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private TrackStudyPointRepository trackStudyPointRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackPage trackPage;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        trackPageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        TrackMaster backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        trackPage = trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());

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
    }

    @Test
    @DisplayName("AC-1.7 전체 교체 후 display_order가 0부터 배열 순서대로 재부여된다")
    void 전체_교체_후_순서가_배열_순서대로_재부여된다() throws Exception {
        String requestBody = """
                {"studyPoints":[
                    {"title":"확장성 있는 엔지니어링","description":"React 컴포넌트 설계"},
                    {"title":"사용자 경험","description":"접근성과 성능"}
                ]}""";

        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("확장성 있는 엔지니어링"))
                .andExpect(jsonPath("$[1].title").value("사용자 경험"));

        var saved = trackStudyPointRepository.findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPage.getId());
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getDisplayOrder()).isZero();
        assertThat(saved.get(1).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("전체 교체는 기존 카드를 대체한다 (덧붙이지 않는다)")
    void 전체_교체는_기존_카드를_대체한다() throws Exception {
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studyPoints\":[{\"title\":\"A\",\"description\":\"a\"}]}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studyPoints\":[{\"title\":\"B\",\"description\":\"b\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("B"));
    }

    @Test
    @DisplayName("AC-1.6 5개를 보내면 400이고 아무것도 저장되지 않는다")
    void 다섯개를_보내면_400() throws Exception {
        String fiveItems = """
                {"studyPoints":[
                    {"title":"1","description":"d"},{"title":"2","description":"d"},
                    {"title":"3","description":"d"},{"title":"4","description":"d"},
                    {"title":"5","description":"d"}
                ]}""";

        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fiveItems))
                .andExpect(status().isBadRequest());

        assertThat(
                trackStudyPointRepository.findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPage.getId())).isEmpty();
    }

    @Test
    @DisplayName("빈 배열이면 카드를 전부 지운다")
    void 빈_배열이면_전부_지운다() throws Exception {
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studyPoints\":[{\"title\":\"A\",\"description\":\"a\"}]}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studyPoints\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("공개 트랙 상세 응답에 studyPoints가 순서대로 포함된다")
    void 공개_응답에_studyPoints가_포함된다() throws Exception {
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/study-points")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studyPoints\":[{\"title\":\"A\",\"description\":\"a\"}]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/tracks/backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyPoints[0].title").value("A"))
                .andExpect(jsonPath("$.studyPoints[0].description").value("a"));
    }
}
