package com.bcsdlab.bcsdinternalapiv2.activity.service;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityCategoryResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.repository.ActivityCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityCategoryService {

    private final ActivityCategoryRepository activityCategoryRepository;

    public List<ActivityCategoryResponse> getCategories() {
        return activityCategoryRepository.findAllByPublishedTrueOrderByDisplayOrderAsc().stream()
                .map(ActivityCategoryResponse::from)
                .toList();
    }
}
