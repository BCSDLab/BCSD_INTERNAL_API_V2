package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCategoryCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCategoryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivityCategoryResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.service.AdminActivityCategoryService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/activity-categories")
@RequiredArgsConstructor
public class AdminActivityCategoryController implements AdminActivityCategoryApi {

    private final AdminActivityCategoryService adminActivityCategoryService;

    @Override
    @GetMapping
    public List<AdminActivityCategoryResponse> getCategories() {
        return adminActivityCategoryService.getCategories();
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminActivityCategoryResponse> createCategory(
            @Valid @RequestBody ActivityCategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminActivityCategoryService.createCategory(request));
    }

    @Override
    @PutMapping("/{id}")
    public AdminActivityCategoryResponse updateCategory(@PathVariable Long id,
                                                         @Valid @RequestBody ActivityCategoryUpdateRequest request) {
        return adminActivityCategoryService.updateCategory(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminActivityCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request) {
        adminActivityCategoryService.publish(id, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request) {
        adminActivityCategoryService.reorder(request);
        return ResponseEntity.noContent().build();
    }
}
