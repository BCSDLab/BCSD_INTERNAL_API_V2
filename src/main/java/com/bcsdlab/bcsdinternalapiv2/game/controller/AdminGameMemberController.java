package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameMembersAttachRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.game.service.AdminGameMemberService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/games/{gameId}/members")
@RequiredArgsConstructor
public class AdminGameMemberController implements AdminGameMemberApi {

    private final AdminGameMemberService adminGameMemberService;

    @Override
    @GetMapping
    public List<AdminGameMemberResponse> getMembers(@PathVariable Long gameId) {
        return adminGameMemberService.getMembers(gameId);
    }

    @Override
    @PostMapping
    public List<AdminGameMemberResponse> attachMembers(@PathVariable Long gameId,
                                                        @Valid @RequestBody GameMembersAttachRequest request) {
        return adminGameMemberService.attachMembers(gameId, request);
    }

    @Override
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> detachMember(@PathVariable Long gameId, @PathVariable Long memberId) {
        adminGameMemberService.detachMember(gameId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@PathVariable Long gameId, @Valid @RequestBody OrderRequest request) {
        adminGameMemberService.reorder(gameId, request);
        return ResponseEntity.noContent().build();
    }
}
