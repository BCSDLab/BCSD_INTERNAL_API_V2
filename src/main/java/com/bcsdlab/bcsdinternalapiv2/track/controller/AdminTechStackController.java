package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TechStackCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TechStackResponse;
import com.bcsdlab.bcsdinternalapiv2.track.service.AdminTechStackService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/tech-stacks")
@RequiredArgsConstructor
public class AdminTechStackController implements AdminTechStackApi {

    private final AdminTechStackService adminTechStackService;

    @Override
    @GetMapping
    public List<TechStackResponse> getTechStacks() {
        return adminTechStackService.getTechStacks();
    }

    @Override
    @PostMapping
    public ResponseEntity<TechStackResponse> createTechStack(@Valid @RequestBody TechStackCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTechStackService.createTechStack(request));
    }
}
