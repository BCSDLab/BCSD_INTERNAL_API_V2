package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.CurriculumUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumTreeResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumService;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumTreeService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/curriculums")
@RequiredArgsConstructor
public class AdminCurriculumController implements AdminCurriculumApi {

    private final AdminCurriculumService adminCurriculumService;
    private final AdminCurriculumTreeService adminCurriculumTreeService;

    @Override
    @GetMapping("/{id}")
    public AdminCurriculumTreeResponse getTree(@PathVariable Long id) {
        return adminCurriculumTreeService.getTree(id);
    }

    @Override
    @PutMapping("/{id}")
    public AdminCurriculumSummaryResponse updateCurriculum(@PathVariable Long id,
                                                            @Valid @RequestBody CurriculumUpdateRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return adminCurriculumService.updateCurriculum(id, request, Long.valueOf(jwt.getSubject()));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurriculum(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        adminCurriculumService.deleteCurriculum(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        adminCurriculumService.publish(id, request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
