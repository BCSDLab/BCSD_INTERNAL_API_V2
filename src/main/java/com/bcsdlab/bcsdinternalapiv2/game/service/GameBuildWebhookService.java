package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.game.config.GameBuildProperties;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildWebhookRequest;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameBuildRepository;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈페이지 서버가 업로드 처리 결과를 통지하는 완료 웹훅 처리(ADR-024). 시크릿
 * 검증만이 인증이다 — {@code /v1/games/**}는 Security 매처가 permitAll이다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GameBuildWebhookService {

    private final GameBuildRepository gameBuildRepository;
    private final GameBuildProperties gameBuildProperties;
    private final ContentChangedPublisher contentChangedPublisher;

    public void applyResult(Long buildId, String secretHeader, GameBuildWebhookRequest request) {
        if (!gameBuildProperties.secret().equals(secretHeader)) {
            throw new GameException(GameExceptionType.GAME_BUILD_WEBHOOK_UNAUTHORIZED);
        }

        GameBuild build = gameBuildRepository.findById(buildId)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_BUILD_NOT_FOUND));

        switch (request.status()) {
            case "ACTIVE" -> applyActive(build, request);
            case "FAILED" -> build.markFailed(request.failureReason());
            default -> throw new GameException(GameExceptionType.GAME_BUILD_INVALID_WEBHOOK_STATUS);
        }
    }

    private void applyActive(GameBuild build, GameBuildWebhookRequest request) {
        build.applyActive(request.canvasWidth(), request.canvasHeight(), request.storageBytes(),
                request.buildFileUrl());

        Long gameId = build.getGame().getId();
        gameBuildRepository.findAllByGame_IdAndStatus(gameId, GameBuildStatus.ACTIVE).stream()
                .filter(other -> !other.getId().equals(build.getId()))
                .forEach(other -> other.updateStatus(GameBuildStatus.ARCHIVED));

        contentChangedPublisher.gameChanged(build.getGame().getSlug());
    }
}
