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
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;
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

class AdminCurriculumTreeIntegrationTest extends IntegrationTestSupport {

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

    private Curriculum curriculum;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        trackPageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        TrackMaster backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        TrackPage trackPage = trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());
        curriculum = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("비기너").published(true).displayOrder(0).build());

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
    @DisplayName("AC-2.5 weekTo가 weekFrom보다 작으면 400이다")
    void 주차_범위가_거꾸로면_400() throws Exception {
        mockMvc.perform(post("/v1/admin/curriculums/" + curriculum.getId() + "/weeks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekFrom\":5,\"weekTo\":3}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("범위 주차를 만들고 라벨을 수정할 수 있다")
    void 범위_주차_생성과_수정() throws Exception {
        String body = mockMvc.perform(post("/v1/admin/curriculums/" + curriculum.getId() + "/weeks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekFrom\":13,\"weekTo\":16}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weekFrom").value(13))
                .andExpect(jsonPath("$.weekTo").value(16))
                .andReturn().getResponse().getContentAsString();
        long weekId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(put("/v1/admin/weeks/" + weekId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekFrom\":14,\"weekTo\":17}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekFrom").value(14))
                .andExpect(jsonPath("$.weekTo").value(17));
    }

    @Test
    @DisplayName("AC-2.3 주차를 삭제하면 하위 토픽·세부항목이 cascade로 삭제되고 다른 주차는 영향받지 않는다")
    void 주차_삭제는_하위_트리에만_cascade된다() throws Exception {
        CurriculumWeek week1 = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(curriculum).weekFrom(1).displayOrder(0).build());
        CurriculumWeek week2 = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(curriculum).weekFrom(2).displayOrder(1).build());
        CurriculumTopic topic1 = curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week1).title("토픽1").displayOrder(0).build());
        curriculumTopicRepository.save(CurriculumTopic.builder().week(week2).title("토픽2").displayOrder(0).build());

        mockMvc.perform(delete("/v1/admin/weeks/" + week1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(curriculumWeekRepository.findById(week1.getId())).isEmpty();
        assertThat(curriculumTopicRepository.findById(topic1.getId())).isEmpty();
        assertThat(curriculumWeekRepository.findById(week2.getId())).isPresent();
        assertThat(curriculumTopicRepository.findAllByWeek_IdOrderByDisplayOrderAsc(week2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("AC-2.6 토픽 순서를 바꾸면 3단 트리 조회에서 순서가 바뀐다")
    void 토픽_순서_변경이_트리_조회에_반영된다() throws Exception {
        CurriculumWeek week = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(curriculum).weekFrom(1).displayOrder(0).build());
        CurriculumTopic first = curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week).title("첫번째").displayOrder(0).build());
        CurriculumTopic second = curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week).title("두번째").displayOrder(1).build());

        mockMvc.perform(patch("/v1/admin/weeks/" + week.getId() + "/topics/order")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[%d,%d]}".formatted(second.getId(), first.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/curriculums/" + curriculum.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeks[0].topics[0].title").value("두번째"))
                .andExpect(jsonPath("$.weeks[0].topics[1].title").value("첫번째"));
    }

    @Test
    @DisplayName("AC-2.9 세부 항목 빈 배열은 에러가 아니라 전부 삭제한다")
    void 빈_배열_전송_시_세부항목_전부_삭제() throws Exception {
        CurriculumWeek week = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(curriculum).weekFrom(1).displayOrder(0).build());
        CurriculumTopic topic = curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week).title("JS 비동기").displayOrder(0).build());

        mockMvc.perform(put("/v1/admin/topics/" + topic.getId() + "/details")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contents\":[\"callback\",\"Promise\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(put("/v1/admin/topics/" + topic.getId() + "/details")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contents\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        assertThat(curriculumTopicDetailRepository.findAllByTopic_IdOrderByDisplayOrderAsc(topic.getId())).isEmpty();
    }

    @Test
    @DisplayName("3단 트리 조회에 토픽이 없는 주차도 포함된다 (공개 API의 제외 규칙과 다르다 — T-12에서 적용)")
    void 관리자_트리_조회는_빈_주차도_포함한다() throws Exception {
        curriculumWeekRepository.save(CurriculumWeek.builder().curriculum(curriculum).weekFrom(1).displayOrder(0)
                .build());

        mockMvc.perform(get("/v1/admin/curriculums/" + curriculum.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeks.length()").value(1))
                .andExpect(jsonPath("$.weeks[0].topics.length()").value(0));
    }
}
