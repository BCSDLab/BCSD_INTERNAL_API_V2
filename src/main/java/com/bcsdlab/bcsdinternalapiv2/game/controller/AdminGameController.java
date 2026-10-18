package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameRatingRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameScreenshotsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameSlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameRatingResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameScreenshotResponse;
import com.bcsdlab.bcsdinternalapiv2.game.service.AdminGameService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/games")
@RequiredArgsConstructor
public class AdminGameController implements AdminGameApi {

    private final AdminGameService adminGameService;

    @Override
    @GetMapping
    public ResponseEntity<List<AdminGameSummaryResponse>> getGames() {
        return ResponseEntity.ok(adminGameService.getGames());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AdminGameDetailResponse> getGame(@PathVariable Long id) {
        return ResponseEntity.ok(adminGameService.getGame(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminGameDetailResponse> createGame(@Valid @RequestBody GameCreateRequest request,
                                                                @LoginMemberId Long loginMemberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminGameService.createGame(request, loginMemberId));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AdminGameDetailResponse> updateGame(@PathVariable Long id, @Valid @RequestBody GameUpdateRequest request,
                                                               @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminGameService.updateGame(id, request, loginMemberId));
    }

    @Override
    @PatchMapping("/{id}/slug")
    public ResponseEntity<AdminGameDetailResponse> changeSlug(@PathVariable Long id,
                                                               @Valid @RequestBody GameSlugChangeRequest request,
                                                               @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminGameService.changeSlug(id, request, loginMemberId));
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request,
                                         @LoginMemberId Long loginMemberId) {
        adminGameService.publish(id, request, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request,
                                         @LoginMemberId Long loginMemberId) {
        adminGameService.reorder(request, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id, @LoginMemberId Long loginMemberId) {
        adminGameService.deleteGame(id, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/screenshots")
    public ResponseEntity<List<GameScreenshotResponse>> replaceScreenshots(@PathVariable Long id,
                                                                            @Valid @RequestBody GameScreenshotsReplaceRequest request,
                                                                            @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminGameService.replaceScreenshots(id, request, loginMemberId));
    }

    @Override
    @PutMapping("/{id}/rating")
    public ResponseEntity<GameRatingResponse> upsertRating(@PathVariable Long id, @Valid @RequestBody GameRatingRequest request,
                                                            @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminGameService.upsertRating(id, request, loginMemberId));
    }

    @Override
    @DeleteMapping("/{id}/rating")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id, @LoginMemberId Long loginMemberId) {
        adminGameService.deleteRating(id, loginMemberId);
        return ResponseEntity.noContent().build();
    }
}
