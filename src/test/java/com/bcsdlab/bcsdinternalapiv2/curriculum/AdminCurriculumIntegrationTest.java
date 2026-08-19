package com.bcsdlab.bcsdinternalapiv2.curriculum;

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
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminCurriculumIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private CurriculumRepository curriculumRepository;

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
    @DisplayName("세트를 생성하면 비공개 상태로 만들어진다")
    void 세트_생성은_비공개_상태다() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/curriculums")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"비기너\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("비기너"))
                .andExpect(jsonPath("$.isPublished").value(false));
    }

    @Test
    @DisplayName("AC-2.1 세트를 공개하면 같은 트랙의 기존 공개 세트가 자동으로 비공개된다")
    void 공개하면_기존_공개_세트가_자동으로_내려간다() throws Exception {
        Curriculum a = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("A").published(true).displayOrder(0).build());
        Curriculum b = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("B").published(false).displayOrder(1).build());

        mockMvc.perform(patch("/v1/admin/curriculums/" + b.getId() + "/publish")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublished\":true}"))
                .andExpect(status().isNoContent());

        assertThat(curriculumRepository.findById(a.getId()).orElseThrow().isPublished()).isFalse();
        assertThat(curriculumRepository.findById(b.getId()).orElseThrow().isPublished()).isTrue();
    }

    @Test
    @DisplayName("이름 수정과 soft delete가 반영된다")
    void 이름_수정과_삭제() throws Exception {
        Curriculum curriculum = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("초안").published(false).displayOrder(0).build());

        mockMvc.perform(put("/v1/admin/curriculums/" + curriculum.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"비기너\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("비기너"));

        mockMvc.perform(delete("/v1/admin/curriculums/" + curriculum.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/track-pages/" + trackPage.getId() + "/curriculums")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
