package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.MemberDirectoryQuery;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberDirectoryResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/members/directory")
@RequiredArgsConstructor
public class MemberDirectoryController implements MemberDirectoryApi {

    private final MemberDirectoryService memberDirectoryService;

    @Override
    @GetMapping
    public ResponseEntity<MemberDirectoryResponse> getDirectory(
            @ModelAttribute MemberDirectoryQuery query,
            @PageableDefault(size = 8, sort = "generation", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(memberDirectoryService.getDirectory(query, pageable));
    }
}
