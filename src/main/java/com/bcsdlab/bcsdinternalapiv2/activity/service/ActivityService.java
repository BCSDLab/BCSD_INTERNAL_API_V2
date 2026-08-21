package com.bcsdlab.bcsdinternalapiv2.activity.service;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityListItemResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityTimelineResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityException;
import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityExceptionType;
import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityImage;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityImageRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 활동 타임라인(FR-5.1)이 쓰는 공개 조회. 연도 그룹 개수와 무관하게 항상 쿼리 3개
 * (활동 목록, 이미지 IN 절 배치 조회 각 1회 + 카테고리 조회 1회)로 끝낸다 — 홈페이지 빌드가
 * 매번 호출하는 공개 경로라 {@link com.bcsdlab.bcsdinternalapiv2.curriculum.service.CurriculumQueryService}와
 * 같은 이유로 N+1을 명시적으로 피한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityImageRepository activityImageRepository;
    private final ActivityCategoryRepository activityCategoryRepository;

    public List<ActivityTimelineResponse> getTimeline(String categorySlug) {
        ActivityCategory category = activityCategoryRepository.findBySlugAndPublishedTrue(categorySlug)
                .orElseThrow(() -> new ActivityException(ActivityExceptionType.CATEGORY_NOT_FOUND));

        List<Activity> activities = activityRepository
                .findAllByCategory_IdAndPublishedTrueOrderByYearDescMonthDescDisplayOrderAsc(category.getId());
        List<Integer> years = activities.stream().map(Activity::getYear).distinct().toList();
        Map<Integer, List<Activity>> activitiesByYear = activities.stream()
                .collect(Collectors.groupingBy(Activity::getYear));

        Map<Long, List<String>> imagesByActivityId = imagesByActivityId(activities);

        return years.stream()
                .map(year -> new ActivityTimelineResponse(year, activitiesByYear.get(year).stream()
                        .map(activity -> ActivityListItemResponse.of(
                                activity, imagesByActivityId.getOrDefault(activity.getId(), List.of())))
                        .toList()))
                .toList();
    }

    public ActivityDetailResponse getActivity(Long id) {
        Activity activity = activityRepository.findByIdAndPublishedTrue(id)
                .orElseThrow(() -> new ActivityException(ActivityExceptionType.ACTIVITY_NOT_FOUND));
        List<String> images = activityImageRepository.findAllByActivity_IdOrderByDisplayOrderAsc(id).stream()
                .map(ActivityImage::getImageUrl)
                .toList();
        return ActivityDetailResponse.of(activity, images);
    }

    private Map<Long, List<String>> imagesByActivityId(List<Activity> activities) {
        List<Long> activityIds = activities.stream().map(Activity::getId).toList();
        List<ActivityImage> images = activityIds.isEmpty()
                ? List.of() : activityImageRepository.findAllByActivity_IdInOrderByDisplayOrderAsc(activityIds);
        return images.stream()
                .collect(Collectors.groupingBy(image -> image.getActivity().getId(),
                        Collectors.mapping(ActivityImage::getImageUrl, Collectors.toList())));
    }
}
