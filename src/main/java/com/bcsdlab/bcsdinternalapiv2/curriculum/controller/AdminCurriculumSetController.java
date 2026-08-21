package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.CurriculumCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/track-pages/{trackPageId}/curriculums")
@RequiredArgsConstructor
public class AdminCurriculumSetController implements AdminCurriculumSetApi {

    private final AdminCurriculumService adminCurriculumService;

    @Override
    @GetMapping
    public List<AdminCurriculumSummaryResponse> getCurriculums(@PathVariable Long trackPageId) {
        return adminCurriculumService.getCurriculums(trackPageId);
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminCurriculumSummaryResponse> createCurriculum(
            @PathVariable Long trackPageId, @Valid @RequestBody CurriculumCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCurriculumService.createCurriculum(trackPageId, request));
    }
}
