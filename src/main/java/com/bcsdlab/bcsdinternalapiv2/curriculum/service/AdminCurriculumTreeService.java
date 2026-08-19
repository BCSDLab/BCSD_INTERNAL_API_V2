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
        return CurriculumWeekResponse.from(week);
    }

    @Transactional
    public CurriculumWeekResponse updateWeek(Long weekId, WeekRequest request) {
        validateRange(request);
        CurriculumWeek week = findWeekOrThrow(weekId);
        week.updateLabel(request.weekFrom(), request.weekTo());
        return CurriculumWeekResponse.from(week);
    }

    @Transactional
    public void deleteWeek(Long weekId) {
        curriculumWeekRepository.delete(findWeekOrThrow(weekId));
    }

    @Transactional
    public void reorderWeeks(Long curriculumId, OrderRequest request) {
        List<CurriculumWeek> weeks = curriculumWeekRepository
                .findAllByCurriculum_IdOrderByDisplayOrderAsc(curriculumId);
        Map<Long, CurriculumWeek> byId = weeks.stream()
                .collect(Collectors.toMap(CurriculumWeek::getId, week -> week));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
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
        return CurriculumTopicResponse.from(topic);
    }

    @Transactional
    public CurriculumTopicResponse updateTopic(Long topicId, TopicRequest request) {
        CurriculumTopic topic = findTopicOrThrow(topicId);
        topic.updateTitle(request.title());
        return CurriculumTopicResponse.from(topic);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        curriculumTopicRepository.delete(findTopicOrThrow(topicId));
    }

    @Transactional
    public void reorderTopics(Long weekId, OrderRequest request) {
        List<CurriculumTopic> topics = curriculumTopicRepository.findAllByWeek_IdOrderByDisplayOrderAsc(weekId);
        Map<Long, CurriculumTopic> byId = topics.stream()
                .collect(Collectors.toMap(CurriculumTopic::getId, topic -> topic));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
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
        return saved.stream().map(CurriculumTopicDetail::getContent).toList();
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
