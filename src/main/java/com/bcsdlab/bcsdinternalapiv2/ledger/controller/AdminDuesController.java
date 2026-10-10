package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesLinkBulkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.SemesterCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.DuesLinkBulkResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterCreatableResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesLinkService;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesQueryService;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/dues/semesters")
@RequiredArgsConstructor
public class AdminDuesController implements AdminDuesApi {

    private final DuesQueryService duesQueryService;
    private final SemesterService semesterService;
    private final DuesLinkService duesLinkService;

    @Override
    @GetMapping
    public ResponseEntity<SemesterDuesListResponse> getSemesters() {
        return ResponseEntity.ok(duesQueryService.getSemesters());
    }

    @Override
    @GetMapping("/creatable")
    public ResponseEntity<SemesterCreatableResponse> getCreatable() {
        return ResponseEntity.ok(semesterService.getCreatable());
    }

    @Override
    @PostMapping
    public ResponseEntity<SemesterDuesSummaryResponse> createSemester(@Valid @RequestBody SemesterCreateRequest request,
                                                                       @AuthenticationPrincipal Jwt jwt) {
        SemesterDuesSummaryResponse created = semesterService.create(request, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @GetMapping("/{semesterId}/members")
    public ResponseEntity<SemesterDuesDetailResponse> getSemesterMembers(@PathVariable String semesterId) {
        return ResponseEntity.ok(duesQueryService.getDetail(semesterId));
    }

    @Override
    @PostMapping("/{semesterId}/links")
    public ResponseEntity<DuesLinkBulkResponse> linkEntries(@PathVariable String semesterId,
                                                            @Valid @RequestBody DuesLinkBulkRequest request,
                                                            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(duesLinkService.linkAll(semesterId, request, Long.valueOf(jwt.getSubject())));
    }
}
