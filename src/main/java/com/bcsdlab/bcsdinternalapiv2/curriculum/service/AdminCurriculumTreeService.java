package com.bcsdlab.bcsdinternalapiv2.curriculum.service;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicDetailsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.WeekRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumTreeResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumTreeResponse.TopicNode;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumTreeResponse.WeekNode;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumTopicResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumWeekResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.exception.CurriculumException;
import com.bcsdlab.bcsdinternalapiv2.curriculum.exception.CurriculumExceptionType;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopic;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumTopicDetail;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.CurriculumWeek;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicDetailRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumTopicRepository;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumWeekRepository;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCurriculumTreeService {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumWeekRepository curriculumWeekRepository;
    private final CurriculumTopicRepository curriculumTopicRepository;
    private final CurriculumTopicDetailRepository curriculumTopicDetailRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public AdminCurriculumTreeResponse getTree(Long curriculumId) {
        Curriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new CurriculumException(CurriculumExceptionType.CURRICULUM_NOT_FOUND));

        List<WeekNode> weeks = curriculumWeekRepository
                .findAllByCurriculum_IdOrderByDisplayOrderAsc(curriculumId).stream()
                .map(week -> {
                    List<TopicNode> topics = curriculumTopicRepository
                            .findAllByWeek_IdOrderByDisplayOrderAsc(week.getId()).stream()
                            .map(topic -> {
                                List<String> details = curriculumTopicDetailRepository
                                        .findAllByTopic_IdOrderByDisplayOrderAsc(topic.getId()).stream()
                                        .map(CurriculumTopicDetail::getContent)
                                        .toList();
                                return new TopicNode(topic.getId(), topic.getTitle(), topic.getDisplayOrder(),
                                        details);
                            })
                            .toList();
                    return new WeekNode(week.getId(), week.getWeekFrom(), week.getWeekTo(), week.getDisplayOrder(),
                            topics);
                })
                .toList();

        return AdminCurriculumTreeResponse.of(curriculum, weeks);
    }

    @Transactional
    public CurriculumWeekResponse createWeek(Long curriculumId, WeekRequest request) {
        validateRange(request);
        Curriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new CurriculumException(CurriculumExceptionType.CURRICULUM_NOT_FOUND));

        int displayOrder = curriculumWeekRepository.findAllByCurriculum_IdOrderByDisplayOrderAsc(curriculumId).size();
        CurriculumWeek week = curriculumWeekRepository.save(CurriculumWeek.builder()
                .curriculum(curriculum)
                .weekFrom(request.weekFrom())
                .weekTo(request.weekTo())
                .displayOrder(displayOrder)
                .build());
        notifyIfPublished(curriculum);
        return CurriculumWeekResponse.from(week);
    }

    @Transactional
    public CurriculumWeekResponse updateWeek(Long weekId, WeekRequest request) {
        validateRange(request);
        CurriculumWeek week = findWeekOrThrow(weekId);
        week.updateLabel(request.weekFrom(), request.weekTo());
        notifyIfPublished(week.getCurriculum());
        return CurriculumWeekResponse.from(week);
    }

    @Transactional
    public void deleteWeek(Long weekId) {
        CurriculumWeek week = findWeekOrThrow(weekId);
        curriculumWeekRepository.delete(week);
        notifyIfPublished(week.getCurriculum());
    }

    @Transactional
    public void reorderWeeks(Long curriculumId, OrderRequest request) {
        List<CurriculumWeek> weeks = curriculumWeekRepository
                .findAllByCurriculum_IdOrderByDisplayOrderAsc(curriculumId);
        Map<Long, CurriculumWeek> byId = weeks.stream()
                .collect(Collectors.toMap(CurriculumWeek::getId, week -> week));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
        if (!weeks.isEmpty()) {
            notifyIfPublished(weeks.get(0).getCurriculum());
        }
    }

    @Transactional
    public CurriculumTopicResponse createTopic(Long weekId, TopicRequest request) {
        CurriculumWeek week = findWeekOrThrow(weekId);
        int displayOrder = curriculumTopicRepository.findAllByWeek_IdOrderByDisplayOrderAsc(weekId).size();
        CurriculumTopic topic = curriculumTopicRepository.save(CurriculumTopic.builder()
                .week(week)
                .title(request.title())
                .displayOrder(displayOrder)
                .build());
        notifyIfPublished(week.getCurriculum());
        return CurriculumTopicResponse.from(topic);
    }

    @Transactional
    public CurriculumTopicResponse updateTopic(Long topicId, TopicRequest request) {
        CurriculumTopic topic = findTopicOrThrow(topicId);
        topic.updateTitle(request.title());
        notifyIfPublished(topic.getWeek().getCurriculum());
        return CurriculumTopicResponse.from(topic);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        CurriculumTopic topic = findTopicOrThrow(topicId);
        curriculumTopicRepository.delete(topic);
        notifyIfPublished(topic.getWeek().getCurriculum());
    }

    @Transactional
    public void reorderTopics(Long weekId, OrderRequest request) {
        List<CurriculumTopic> topics = curriculumTopicRepository.findAllByWeek_IdOrderByDisplayOrderAsc(weekId);
        Map<Long, CurriculumTopic> byId = topics.stream()
                .collect(Collectors.toMap(CurriculumTopic::getId, topic -> topic));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
        if (!topics.isEmpty()) {
            notifyIfPublished(topics.get(0).getWeek().getCurriculum());
        }
    }

    @Transactional
    public List<String> replaceDetails(Long topicId, TopicDetailsReplaceRequest request) {
        CurriculumTopic topic = findTopicOrThrow(topicId);
        curriculumTopicDetailRepository.deleteAllByTopic_Id(topicId);

        List<String> contents = request.contents();
        List<CurriculumTopicDetail> saved = new ArrayList<>(contents.size());
        for (int i = 0; i < contents.size(); i++) {
            saved.add(curriculumTopicDetailRepository.save(CurriculumTopicDetail.builder()
                    .topic(topic)
                    .content(contents.get(i))
                    .displayOrder(i)
                    .build()));
        }
        notifyIfPublished(topic.getWeek().getCurriculum());
        return saved.stream().map(CurriculumTopicDetail::getContent).toList();
    }

    /**
     * source의 주차 &gt; 토픽 &gt; 세부항목을 전부 target 아래로 복제한다(AC-2.8). id는
     * 새로 발급되지만 개수·순서(display_order)는 원본과 동일하게 유지한다.
     */
    @Transactional
    public void cloneTree(Curriculum source, Curriculum target) {
        for (CurriculumWeek sourceWeek : curriculumWeekRepository
                .findAllByCurriculum_IdOrderByDisplayOrderAsc(source.getId())) {
            CurriculumWeek clonedWeek = curriculumWeekRepository.save(CurriculumWeek.builder()
                    .curriculum(target)
                    .weekFrom(sourceWeek.getWeekFrom())
                    .weekTo(sourceWeek.getWeekTo())
                    .displayOrder(sourceWeek.getDisplayOrder())
                    .build());

            for (CurriculumTopic sourceTopic : curriculumTopicRepository
                    .findAllByWeek_IdOrderByDisplayOrderAsc(sourceWeek.getId())) {
                CurriculumTopic clonedTopic = curriculumTopicRepository.save(CurriculumTopic.builder()
                        .week(clonedWeek)
                        .title(sourceTopic.getTitle())
                        .displayOrder(sourceTopic.getDisplayOrder())
                        .build());

                for (CurriculumTopicDetail sourceDetail : curriculumTopicDetailRepository
                        .findAllByTopic_IdOrderByDisplayOrderAsc(sourceTopic.getId())) {
                    curriculumTopicDetailRepository.save(CurriculumTopicDetail.builder()
                            .topic(clonedTopic)
                            .content(sourceDetail.getContent())
                            .displayOrder(sourceDetail.getDisplayOrder())
                            .build());
                }
            }
        }
    }

    /** 공개 세트 하위 변경만 홈페이지에 영향을 준다(05-api-spec.md §4) — 초안 편집은 조용히 넘어간다. */
    private void notifyIfPublished(Curriculum curriculum) {
        if (curriculum.isPublished()) {
            contentChangedPublisher.trackChanged(curriculum.getTrackPage().getSlug());
        }
    }

    private void validateRange(WeekRequest request) {
        if (request.weekTo() != null && request.weekTo() < request.weekFrom()) {
            throw new CurriculumException(CurriculumExceptionType.WEEK_RANGE_INVALID);
        }
    }

    private CurriculumWeek findWeekOrThrow(Long weekId) {
        return curriculumWeekRepository.findById(weekId)
                .orElseThrow(() -> new CurriculumException(CurriculumExceptionType.WEEK_NOT_FOUND));
    }

    private CurriculumTopic findTopicOrThrow(Long topicId) {
        return curriculumTopicRepository.findById(topicId)
                .orElseThrow(() -> new CurriculumException(CurriculumExceptionType.TOPIC_NOT_FOUND));
    }
}
