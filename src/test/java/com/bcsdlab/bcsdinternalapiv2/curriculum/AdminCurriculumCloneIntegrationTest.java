package com.bcsdlab.bcsdinternalapiv2.curriculum;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopicDetail;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumWeek;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicDetailRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumWeekRepository;
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

class AdminCurriculumCloneIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";

    @Autowired
    private TrackPageRepository trackPageRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private CurriculumRepository curriculumRepository;

    @Autowired
    private CurriculumWeekRepository curriculumWeekRepository;

    @Autowired
    private CurriculumTopicRepository curriculumTopicRepository;

    @Autowired
    private CurriculumTopicDetailRepository curriculumTopicDetailRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackPage trackPage;
    private Curriculum source;
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

        source = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("비기너").published(true).displayOrder(0).build());
        CurriculumWeek week1 = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(source).weekFrom(1).displayOrder(0).build());
        CurriculumWeek week2 = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(source).weekFrom(13).weekTo(16).displayOrder(1).build());
        CurriculumTopic topic1 = curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week1).title("웹 동작 방식").displayOrder(0).build());
        curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week1).title("HTTP").displayOrder(1).build());
        curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week2).title("PROJECT").displayOrder(0).build());
        curriculumTopicDetailRepository.save(
                CurriculumTopicDetail.builder().topic(topic1).content("클라이언트-서버").displayOrder(0).build());
        curriculumTopicDetailRepository.save(
                CurriculumTopicDetail.builder().topic(topic1).content("DNS").displayOrder(1).build());

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
    @DisplayName("AC-2.8 세트를 복제하면 주차·토픽·세부항목 개수와 순서가 원본과 같고 복제본은 비공개다")
    void 세트를_복제하면_트리_전체가_복사되고_비공개다() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/curriculums")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurriculumId\":%d}".formatted(source.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("비기너"))
                .andExpect(jsonPath("$.isPublished").value(false))
                .andReturn().getResponse().getContentAsString();
        long cloneId = objectMapper.readTree(body).get("id").asLong();

        var clonedWeeks = curriculumWeekRepository.findAllByCurriculum_IdOrderByDisplayOrderAsc(cloneId);
        assertThat(clonedWeeks).hasSize(2);
        assertThat(clonedWeeks.get(0).getWeekFrom()).isEqualTo(1);
        assertThat(clonedWeeks.get(1).getWeekFrom()).isEqualTo(13);
        assertThat(clonedWeeks.get(1).getWeekTo()).isEqualTo(16);

        var clonedTopicsOfWeek1 = curriculumTopicRepository
                .findAllByWeek_IdOrderByDisplayOrderAsc(clonedWeeks.get(0).getId());
        assertThat(clonedTopicsOfWeek1).hasSize(2);
        assertThat(clonedTopicsOfWeek1.get(0).getTitle()).isEqualTo("웹 동작 방식");

        var clonedDetails = curriculumTopicDetailRepository
                .findAllByTopic_IdOrderByDisplayOrderAsc(clonedTopicsOfWeek1.get(0).getId());
        assertThat(clonedDetails).hasSize(2);
        assertThat(clonedDetails.get(0).getContent()).isEqualTo("클라이언트-서버");
        assertThat(clonedDetails.get(1).getContent()).isEqualTo("DNS");

        // 원본은 그대로다 — 복제가 원본을 건드리지 않는다
        assertThat(curriculumWeekRepository.findAllByCurriculum_IdOrderByDisplayOrderAsc(source.getId())).hasSize(2);
    }

    @Test
    @DisplayName("이름 없이 복제하면 원본 이름을 그대로 쓴다")
    void 이름_없이_복제하면_원본_이름을_쓴다() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/curriculums")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurriculumId\":%d,\"name\":\"\"}".formatted(source.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("비기너"));
    }

    @Test
    @DisplayName("복제가 아닌데 이름이 없으면 400이다")
    void 복제가_아닌데_이름이_없으면_400() throws Exception {
        mockMvc.perform(post("/v1/admin/track-pages/" + trackPage.getId() + "/curriculums")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
