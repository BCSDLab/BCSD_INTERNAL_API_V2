package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildWebhookRequest;
import com.bcsdlab.bcsdinternalapiv2.game.service.GameBuildWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/games/builds/{buildId}/webhook")
@RequiredArgsConstructor
public class GameBuildWebhookController implements GameBuildWebhookApi {

    private final GameBuildWebhookService gameBuildWebhookService;

    @Override
    @PostMapping
    public ResponseEntity<Void> receiveResult(@PathVariable Long buildId,
                                               @RequestHeader(value = "X-Game-Build-Secret", required = false, defaultValue = "")
                                               String secret,
                                               @Valid @RequestBody GameBuildWebhookRequest request) {
        gameBuildWebhookService.applyResult(buildId, secret, request);
        return ResponseEntity.noContent().build();
    }
}
