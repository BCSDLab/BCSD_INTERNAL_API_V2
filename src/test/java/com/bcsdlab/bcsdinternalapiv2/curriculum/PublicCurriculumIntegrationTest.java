package com.bcsdlab.bcsdinternalapiv2.curriculum;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopicDetail;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumWeek;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicDetailRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumWeekRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PublicCurriculumIntegrationTest extends IntegrationTestSupport {

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

    private TrackMaster backend;

    @BeforeEach
    void setUp() {
        trackPageRepository.deleteAll();
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
    }

    @Test
    @DisplayName("AC-2.2 공개 커리큘럼 세트가 없으면 curriculum은 null이고 500이 아니다")
    void 공개_세트가_없으면_null이다() throws Exception {
        trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());

        mockMvc.perform(get("/v1/tracks/backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curriculum").doesNotExist());
    }

    @Test
    @DisplayName("AC-2.4 weekFrom/weekTo가 그대로 반환된다 · AC-2.7 토픽 0개인 주차는 제외된다")
    void weekFrom_weekTo_그대로_반환하고_빈_주차는_제외한다() throws Exception {
        TrackPage trackPage = trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());
        Curriculum curriculum = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("비기너").published(true).displayOrder(0).build());

        CurriculumWeek weekWithTopic = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(curriculum).weekFrom(4).displayOrder(0).build());
        CurriculumTopic topic = curriculumTopicRepository.save(
                CurriculumTopic.builder().week(weekWithTopic).title("JS 비동기").displayOrder(0).build());
        curriculumTopicDetailRepository.save(
                CurriculumTopicDetail.builder().topic(topic).content("callback").displayOrder(0).build());
        curriculumTopicDetailRepository.save(
                CurriculumTopicDetail.builder().topic(topic).content("Promise").displayOrder(1).build());

        // 토픽이 없는 범위 주차 — 공개 응답에서 제외되어야 한다(AC-2.7)
        curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(curriculum).weekFrom(13).weekTo(16).displayOrder(1).build());

        mockMvc.perform(get("/v1/tracks/backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curriculum.name").value("비기너"))
                .andExpect(jsonPath("$.curriculum.weeks.length()").value(1))
                .andExpect(jsonPath("$.curriculum.weeks[0].weekFrom").value(4))
                .andExpect(jsonPath("$.curriculum.weeks[0].weekTo").doesNotExist())
                .andExpect(jsonPath("$.curriculum.weeks[0].topics[0].title").value("JS 비동기"))
                .andExpect(jsonPath("$.curriculum.weeks[0].topics[0].details[0]").value("callback"))
                .andExpect(jsonPath("$.curriculum.weeks[0].topics[0].details[1]").value("Promise"));
    }

    @Test
    @DisplayName("비공개 세트는 노출되지 않는다")
    void 비공개_세트는_노출되지_않는다() throws Exception {
        TrackPage trackPage = trackPageRepository.save(TrackPage.builder()
                .track(backend).slug("backend").displayName("Backend").tagline("tagline")
                .displayOrder(0).published(true).build());
        Curriculum draft = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage).name("초안").published(false).displayOrder(0).build());
        CurriculumWeek week = curriculumWeekRepository.save(
                CurriculumWeek.builder().curriculum(draft).weekFrom(1).displayOrder(0).build());
        curriculumTopicRepository.save(
                CurriculumTopic.builder().week(week).title("비공개 토픽").displayOrder(0).build());

        mockMvc.perform(get("/v1/tracks/backend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curriculum").doesNotExist());
    }
}
