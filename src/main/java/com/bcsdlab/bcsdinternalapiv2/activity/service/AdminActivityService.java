package com.bcsdlab.bcsdinternalapiv2.activity.service;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityImagesReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivityDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivitySummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityException;
import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityExceptionType;
import com.bcsdlab.bcsdinternalapiv2.activity.model.Activity;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityImage;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityImageRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityRepository;
import com.bcsdlab.bcsdinternalapiv2.activity.util.ActivityContentSanitizer;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityImageRepository activityImageRepository;
    private final ActivityCategoryRepository activityCategoryRepository;

    public Page<AdminActivitySummaryResponse> getActivities(Long categoryId, Integer year, Boolean published,
                                                              Pageable pageable) {
        List<Specification<Activity>> specs = new ArrayList<>();
        if (categoryId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (year != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("year"), year));
        }
        if (published != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("published"), published));
        }
        return activityRepository.findAll(Specification.allOf(specs), pageable)
                .map(AdminActivitySummaryResponse::from);
    }

    public AdminActivityDetailResponse getActivity(Long id) {
        Activity activity = findOrThrow(id);
        return AdminActivityDetailResponse.of(activity, imageUrls(id));
    }

    @Transactional
    public AdminActivityDetailResponse createActivity(ActivityCreateRequest request) {
        ActivityCategory category = activityCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ActivityException(ActivityExceptionType.CATEGORY_NOT_FOUND));

        int displayOrder = activityRepository.findAllByCategory_IdAndYearAndMonthOrderByDisplayOrderAsc(
                request.categoryId(), request.year(), request.month()).size();

        Activity activity = activityRepository.save(Activity.builder()
                .category(category)
                .year(request.year())
                .month(request.month())
                .title(request.title())
                .summary(request.summary())
                .content(ActivityContentSanitizer.sanitize(request.content()))
                .externalUrl(request.externalUrl())
                .displayOrder(displayOrder)
                .published(true)
                .build());
        return AdminActivityDetailResponse.of(activity, List.of());
    }

    @Transactional
    public AdminActivityDetailResponse updateActivity(Long id, ActivityUpdateRequest request) {
        Activity activity = findOrThrow(id);
        activity.updateContent(request.year(), request.month(), request.title(), request.summary(),
                ActivityContentSanitizer.sanitize(request.content()), request.externalUrl());
        return AdminActivityDetailResponse.of(activity, imageUrls(id));
    }

    @Transactional
    public void deleteActivity(Long id) {
        findOrThrow(id).delete(Instant.now());
    }

    @Transactional
    public void publish(Long id, PublishRequest request) {
        findOrThrow(id).updatePublished(request.isPublished());
    }

    @Transactional
    public void reorder(Long categoryId, int year, int month, OrderRequest request) {
        List<Activity> activities = activityRepository
                .findAllByCategory_IdAndYearAndMonthOrderByDisplayOrderAsc(categoryId, year, month);
        Map<Long, Activity> byId = activities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
    }

    @Transactional
    public List<String> replaceImages(Long id, ActivityImagesReplaceRequest request) {
        Activity activity = findOrThrow(id);
        activityImageRepository.deleteAllByActivity_Id(id);

        List<String> urls = request.imageUrls();
        List<ActivityImage> saved = new ArrayList<>(urls.size());
        for (int i = 0; i < urls.size(); i++) {
            saved.add(activityImageRepository.save(ActivityImage.builder()
                    .activity(activity)
                    .imageUrl(urls.get(i))
                    .displayOrder(i)
                    .build()));
        }
        return saved.stream().map(ActivityImage::getImageUrl).toList();
    }

    private List<String> imageUrls(Long activityId) {
        return activityImageRepository.findAllByActivity_IdOrderByDisplayOrderAsc(activityId).stream()
                .map(ActivityImage::getImageUrl)
                .toList();
    }

    private Activity findOrThrow(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ActivityException(ActivityExceptionType.ACTIVITY_NOT_FOUND));
    }
}
