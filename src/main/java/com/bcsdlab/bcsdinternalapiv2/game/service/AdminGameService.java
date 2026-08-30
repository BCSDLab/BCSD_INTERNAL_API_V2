package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.activity.util.ActivityContentSanitizer;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameSlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.global.util.SlugGenerator;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임 기본정보·설명의 관리자 CRUD(T-37). 트랙 페이지(T-07)와 같은 패턴 —
 * 스크린샷·등급정보·참여멤버는 이 서비스가 다루지 않는다(T-38).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGameService {

    private final GameRepository gameRepository;
    private final TrackMasterRepository trackMasterRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public List<AdminGameSummaryResponse> getGames() {
        return gameRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminGameSummaryResponse::from)
                .toList();
    }

    public AdminGameDetailResponse getGame(Long id) {
        return AdminGameDetailResponse.from(findGameOrThrow(id));
    }

    @Transactional
    public AdminGameDetailResponse createGame(GameCreateRequest request) {
        TrackMaster track = findTrackOrNull(request.trackId());

        String slug = SlugGenerator.fromOrFallback(request.name(), "game");
        if (gameRepository.existsBySlug(slug)) {
            throw new GameException(GameExceptionType.GAME_SLUG_DUPLICATED);
        }

        Game game = Game.builder()
                .slug(slug)
                .name(request.name())
                .oneLiner(request.oneLiner())
                .track(track)
                .teamLabel(request.teamLabel())
                .displayOrder((int) gameRepository.count())
                .published(false)
                .build();

        Game saved = gameRepository.save(game);
        contentChangedPublisher.gameAndListChanged(saved.getSlug());
        return AdminGameDetailResponse.from(saved);
    }

    @Transactional
    public AdminGameDetailResponse updateGame(Long id, GameUpdateRequest request) {
        Game game = findGameOrThrow(id);
        TrackMaster track = findTrackOrNull(request.trackId());

        game.updateDetails(track, request.name(), request.oneLiner(), request.teamLabel(),
                ActivityContentSanitizer.sanitize(request.description()));
        contentChangedPublisher.gameChanged(game.getSlug());
        return AdminGameDetailResponse.from(game);
    }

    @Transactional
    public AdminGameDetailResponse changeSlug(Long id, GameSlugChangeRequest request) {
        Game game = findGameOrThrow(id);
        if (!game.getSlug().equals(request.slug()) && gameRepository.existsBySlug(request.slug())) {
            throw new GameException(GameExceptionType.GAME_SLUG_DUPLICATED);
        }

        String oldSlug = game.getSlug();
        game.changeSlug(request.slug());
        contentChangedPublisher.publish(List.of("game-list", "game:" + oldSlug, "game:" + request.slug()));
        return AdminGameDetailResponse.from(game);
    }

    @Transactional
    public void publish(Long id, PublishRequest request) {
        Game game = findGameOrThrow(id);
        game.updatePublished(request.isPublished());
        contentChangedPublisher.gameAndListChanged(game.getSlug());
    }

    @Transactional
    public void reorder(OrderRequest request) {
        List<Game> games = gameRepository.findAll();
        Map<Long, Game> byId = games.stream().collect(Collectors.toMap(Game::getId, game -> game));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((gameId, order) -> byId.get(gameId).updateDisplayOrder(order));
        contentChangedPublisher.gameListChanged();
    }

    @Transactional
    public void deleteGame(Long id) {
        Game game = findGameOrThrow(id);
        game.delete(Instant.now());
        contentChangedPublisher.gameAndListChanged(game.getSlug());
    }

    private Game findGameOrThrow(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_NOT_FOUND));
    }

    private TrackMaster findTrackOrNull(Long trackId) {
        if (trackId == null) {
            return null;
        }
        return trackMasterRepository.findById(trackId)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_NOT_FOUND));
    }
}
