package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicDetailsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumTopicResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumTreeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/topics")
@RequiredArgsConstructor
public class AdminTopicController implements AdminTopicApi {

    private final AdminCurriculumTreeService adminCurriculumTreeService;

    @Override
    @PutMapping("/{id}")
    public CurriculumTopicResponse updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        return adminCurriculumTreeService.updateTopic(id, request, Long.valueOf(jwt.getSubject()));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        adminCurriculumTreeService.deleteTopic(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/details")
    public List<String> replaceDetails(@PathVariable Long id, @Valid @RequestBody TopicDetailsReplaceRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return adminCurriculumTreeService.replaceDetails(id, request, Long.valueOf(jwt.getSubject()));
    }
}
