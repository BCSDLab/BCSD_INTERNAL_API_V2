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
import com.bcsdlab.bcsdinternalapiv2.track.model.TechStack;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TechStackRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageTechStackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class TechStackIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private TechStackRepository techStackRepository;

    @Autowired
    private TrackPageTechStackRepository trackPageTechStackRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackPage trackPage;
    private TechStack react;
    private TechStack java;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        trackPageRepository.deleteAll();
        techStackRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        TrackMaster backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        trackPage = trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());
        react = techStackRepository.save(TechStack.builder().name("React").iconUrl("https://x/react.svg").build());
        java = techStackRepository.save(TechStack.builder().name("Java").iconUrl("https://x/java.svg").build());

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
    @DisplayName("기술스택 마스터 생성 및 이름 중복 409")
    void 기술스택_마스터_생성() throws Exception {
        mockMvc.perform(post("/v1/admin/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Vite\",\"iconUrl\":\"https://x/vite.svg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Vite"));

        mockMvc.perform(post("/v1/admin/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"React\",\"iconUrl\":\"https://x/react-dup.svg\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("트랙 페이지에 기술스택을 순서대로 부착하고 순서를 바꾸면 반영된다")
    void 기술스택을_전체_교체한다() throws Exception {
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"techStackIds\":[%d,%d]}".formatted(react.getId(), java.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("React"))
                .andExpect(jsonPath("$[1].name").value("Java"));

        // 순서를 뒤집어 다시 교체하면 이전 부착이 완전히 대체된다
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"techStackIds\":[%d]}".formatted(java.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"));

        var attached = trackPageTechStackRepository.findAllByTrackPageIdOrderByDisplayOrderAsc(trackPage.getId());
        assertThat(attached).hasSize(1);
        assertThat(attached.get(0).getTechStack().getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("존재하지 않는 기술스택 id를 보내면 404이고 기존 부착이 유지된다")
    void 존재하지_않는_id는_404() throws Exception {
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"techStackIds\":[%d]}".formatted(react.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"techStackIds\":[999999]}"))
                .andExpect(status().isNotFound());

        var attached = trackPageTechStackRepository.findAllByTrackPageIdOrderByDisplayOrderAsc(trackPage.getId());
        assertThat(attached).hasSize(1);
        assertThat(attached.get(0).getTechStack().getName()).isEqualTo("React");
    }

    @Test
    @DisplayName("공개 트랙 상세 응답에 techStacks가 순서대로 포함된다")
    void 공개_응답에_techStacks가_포함된다() throws Exception {
        mockMvc.perform(put("/v1/admin/track-pages/" + trackPage.getId() + "/tech-stacks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"techStackIds\":[%d,%d]}".formatted(java.getId(), react.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/tracks/backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.techStacks[0].name").value("Java"))
                .andExpect(jsonPath("$.techStacks[1].name").value("React"));
    }
}
