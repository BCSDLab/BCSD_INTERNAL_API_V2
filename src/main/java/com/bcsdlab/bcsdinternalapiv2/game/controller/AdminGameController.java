package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameSlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameSummaryResponse;
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
    public List<AdminGameSummaryResponse> getGames() {
        return adminGameService.getGames();
    }

    @Override
    @GetMapping("/{id}")
    public AdminGameDetailResponse getGame(@PathVariable Long id) {
        return adminGameService.getGame(id);
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminGameDetailResponse> createGame(@Valid @RequestBody GameCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminGameService.createGame(request));
    }

    @Override
    @PutMapping("/{id}")
    public AdminGameDetailResponse updateGame(@PathVariable Long id, @Valid @RequestBody GameUpdateRequest request) {
        return adminGameService.updateGame(id, request);
    }

    @Override
    @PatchMapping("/{id}/slug")
    public AdminGameDetailResponse changeSlug(@PathVariable Long id,
                                               @Valid @RequestBody GameSlugChangeRequest request) {
        return adminGameService.changeSlug(id, request);
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request) {
        adminGameService.publish(id, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request) {
        adminGameService.reorder(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        adminGameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}
