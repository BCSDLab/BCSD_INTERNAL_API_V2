package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.ServiceMemberListResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.ServiceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/service/members")
@RequiredArgsConstructor
public class ServiceMemberController implements ServiceMemberApi {

    private final ServiceMemberService serviceMemberService;

    @Override
    @GetMapping
    public ResponseEntity<ServiceMemberListResponse> getMembers() {
        return ResponseEntity.ok(serviceMemberService.getMembers());
    }
}
