package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityCategoryResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.service.ActivityCategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/activity-categories")
@RequiredArgsConstructor
public class ActivityCategoryController implements ActivityCategoryApi {

    private final ActivityCategoryService activityCategoryService;

    @Override
    @GetMapping
    public List<ActivityCategoryResponse> getCategories() {
        return activityCategoryService.getCategories();
    }
}
