package com.bcsdlab.bcsdinternalapiv2.curriculum.service;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse.TopicResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse.WeekResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopicDetail;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumWeek;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicDetailRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumWeekRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공개 트랙 상세(ADR-013)가 쓰는 커리큘럼 조회. 트리 깊이와 무관하게 항상 쿼리 3개
 * (주차/토픽/세부항목 각 1회, IN 절 배치 조회)로 끝낸다 — 관리자용 3단 트리 조회(T-11)는
 * 편집 화면 트래픽이라 N+1을 감수했지만, 이건 홈페이지 빌드가 매번 호출하는 공개
 * 경로라 명시적으로 N+1을 피했다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumQueryService {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumWeekRepository curriculumWeekRepository;
    private final CurriculumTopicRepository curriculumTopicRepository;
    private final CurriculumTopicDetailRepository curriculumTopicDetailRepository;

    public Optional<CurriculumResponse> getPublishedCurriculum(Long trackPageId) {
        Curriculum curriculum = curriculumRepository.findByTrackPage_IdAndPublishedTrue(trackPageId).orElse(null);
        if (curriculum == null) {
            return Optional.empty();
        }

        List<CurriculumWeek> weeks = curriculumWeekRepository
                .findAllByCurriculum_IdOrderByDisplayOrderAsc(curriculum.getId());
        List<Long> weekIds = weeks.stream().map(CurriculumWeek::getId).toList();

        List<CurriculumTopic> topics = weekIds.isEmpty()
                ? List.of() : curriculumTopicRepository.findAllByWeek_IdInOrderByDisplayOrderAsc(weekIds);
        Map<Long, List<CurriculumTopic>> topicsByWeekId = topics.stream()
                .collect(Collectors.groupingBy(topic -> topic.getWeek().getId()));

        List<Long> topicIds = topics.stream().map(CurriculumTopic::getId).toList();
        List<CurriculumTopicDetail> details = topicIds.isEmpty()
                ? List.of() : curriculumTopicDetailRepository.findAllByTopic_IdInOrderByDisplayOrderAsc(topicIds);
        Map<Long, List<CurriculumTopicDetail>> detailsByTopicId = details.stream()
                .collect(Collectors.groupingBy(detail -> detail.getTopic().getId()));

        List<WeekResponse> weekResponses = weeks.stream()
                .map(week -> {
                    List<TopicResponse> topicResponses = topicsByWeekId
                            .getOrDefault(week.getId(), List.of()).stream()
                            .map(topic -> new TopicResponse(
                                    topic.getTitle(),
                                    detailsByTopicId.getOrDefault(topic.getId(), List.of()).stream()
                                            .map(CurriculumTopicDetail::getContent)
                                            .toList()))
                            .toList();
                    return new WeekResponse(week.getWeekFrom(), week.getWeekTo(), topicResponses);
                })
                .filter(week -> !week.topics().isEmpty()) // AC-2.7: 토픽 0개인 주차는 제외
                .toList();

        return Optional.of(new CurriculumResponse(curriculum.getName(), weekResponses));
    }
}
