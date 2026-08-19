package com.bcsdlab.bcsdinternalapiv2.activity.service;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCategoryCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCategoryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivityCategoryResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityException;
import com.bcsdlab.bcsdinternalapiv2.activity.exception.ActivityExceptionType;
import com.bcsdlab.bcsdinternalapiv2.activity.model.ActivityCategory;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminActivityCategoryService {

    private final ActivityCategoryRepository activityCategoryRepository;

    public List<AdminActivityCategoryResponse> getCategories() {
        return activityCategoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminActivityCategoryResponse::from)
                .toList();
    }

    @Transactional
    public AdminActivityCategoryResponse createCategory(ActivityCategoryCreateRequest request) {
        if (activityCategoryRepository.existsBySlug(request.slug())) {
            throw new ActivityException(ActivityExceptionType.CATEGORY_SLUG_DUPLICATED);
        }

        int displayOrder = activityCategoryRepository.findAllByOrderByDisplayOrderAsc().size();
        ActivityCategory category = activityCategoryRepository.save(ActivityCategory.builder()
                .slug(request.slug())
                .name(request.name())
                .headline(request.headline())
                .heroImageUrl(request.heroImageUrl())
                .displayOrder(displayOrder)
                .published(true)
                .build());
        return AdminActivityCategoryResponse.from(category);
    }

    @Transactional
    public AdminActivityCategoryResponse updateCategory(Long id, ActivityCategoryUpdateRequest request) {
        ActivityCategory category = findOrThrow(id);
        category.updateHeader(request.name(), request.headline(), request.heroImageUrl());
        return AdminActivityCategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        findOrThrow(id).delete(Instant.now());
    }

    @Transactional
    public void publish(Long id, PublishRequest request) {
        findOrThrow(id).updatePublished(request.isPublished());
    }

    @Transactional
    public void reorder(OrderRequest request) {
        List<ActivityCategory> categories = activityCategoryRepository.findAllByOrderByDisplayOrderAsc();
        Map<Long, ActivityCategory> byId = categories.stream()
                .collect(Collectors.toMap(ActivityCategory::getId, category -> category));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
    }

    private ActivityCategory findOrThrow(Long id) {
        return activityCategoryRepository.findById(id)
                .orElseThrow(() -> new ActivityException(ActivityExceptionType.CATEGORY_NOT_FOUND));
    }
}
