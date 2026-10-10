package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterAddRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.RosterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterCandidateListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.RosterMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterRosterResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.RosterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/dues/semesters/{semesterId}/roster")
@RequiredArgsConstructor
public class AdminDuesRosterController implements AdminDuesRosterApi {

    private final RosterService rosterService;

    @Override
    @GetMapping
    public ResponseEntity<SemesterRosterResponse> getRoster(@PathVariable String semesterId) {
        return ResponseEntity.ok(rosterService.getRoster(semesterId));
    }

    @Override
    @GetMapping("/candidates")
    public ResponseEntity<RosterCandidateListResponse> getCandidates(@PathVariable String semesterId) {
        return ResponseEntity.ok(rosterService.getCandidates(semesterId));
    }

    @Override
    @PostMapping
    public ResponseEntity<RosterMemberResponse> addRosterMember(@PathVariable String semesterId,
                                                                @Valid @RequestBody RosterAddRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rosterService.add(semesterId, request));
    }

    @Override
    @PatchMapping("/{memberId}")
    public ResponseEntity<RosterMemberResponse> updateRosterMember(@PathVariable String semesterId,
                                                                   @PathVariable Long memberId,
                                                                   @Valid @RequestBody RosterUpdateRequest request) {
        return ResponseEntity.ok(rosterService.update(semesterId, memberId, request));
    }
}
