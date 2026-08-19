package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicDetailsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumTopicResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.AdminCurriculumTreeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public CurriculumTopicResponse updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        return adminCurriculumTreeService.updateTopic(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        adminCurriculumTreeService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/details")
    public List<String> replaceDetails(@PathVariable Long id, @Valid @RequestBody TopicDetailsReplaceRequest request) {
        return adminCurriculumTreeService.replaceDetails(id, request);
    }
}
