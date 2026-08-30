package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameBuildResponse;
import com.bcsdlab.bcsdinternalapiv2.game.service.AdminGameBuildService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/games/{gameId}/builds")
@RequiredArgsConstructor
public class AdminGameBuildController implements AdminGameBuildApi {

    private final AdminGameBuildService adminGameBuildService;

    @Override
    @GetMapping
    public List<AdminGameBuildResponse> getBuilds(@PathVariable Long gameId) {
        return adminGameBuildService.getBuilds(gameId);
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminGameBuildResponse> createBuild(@PathVariable Long gameId,
                                                               @Valid @RequestBody GameBuildCreateRequest request,
                                                               @AuthenticationPrincipal Jwt jwt) {
        Long uploadedByMemberId = Long.valueOf(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminGameBuildService.createBuild(gameId, request, uploadedByMemberId));
    }

    @Override
    @DeleteMapping("/{buildId}")
    public ResponseEntity<Void> deleteBuild(@PathVariable Long gameId, @PathVariable Long buildId) {
        adminGameBuildService.deleteBuild(gameId, buildId);
        return ResponseEntity.noContent().build();
    }
}
