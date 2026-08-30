package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.activity.util.ActivityContentSanitizer;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameRatingRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameScreenshotsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameSlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameRatingResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameScreenshotResponse;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameContentDescriptor;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameRating;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameScreenshot;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRatingRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameScreenshotRepository;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임 기본정보·설명·스크린샷·등급정보의 관리자 CRUD(T-37, T-38). 트랙 페이지(T-07~09)와
 * 같은 패턴 — 참여멤버는 배정/순서 흐름이 따로 있어 {@link AdminGameMemberService}로
 * 분리한다(AdminTrackPageMemberService와 동일한 경계).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGameService {

    private final GameRepository gameRepository;
    private final GameScreenshotRepository gameScreenshotRepository;
    private final GameRatingRepository gameRatingRepository;
    private final GameMemberRepository gameMemberRepository;
    private final TrackMasterRepository trackMasterRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public List<AdminGameSummaryResponse> getGames() {
        return gameRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminGameSummaryResponse::from)
                .toList();
    }

    public AdminGameDetailResponse getGame(Long id) {
        Game game = findGameOrThrow(id);

        List<GameScreenshotResponse> screenshots = gameScreenshotRepository
                .findAllByGame_IdOrderByDisplayOrderAsc(id).stream()
                .map(GameScreenshotResponse::from)
                .toList();
        GameRatingResponse rating = gameRatingRepository.findByGame_Id(id)
                .map(GameRatingResponse::from)
                .orElse(null);
        List<AdminGameMemberResponse> members = gameMemberRepository
                .findAllByGame_IdOrderByDisplayOrderAsc(id).stream()
                .map(AdminGameMemberResponse::from)
                .toList();

        return AdminGameDetailResponse.of(game, screenshots, rating, members);
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

    @Transactional
    public List<GameScreenshotResponse> replaceScreenshots(Long id, GameScreenshotsReplaceRequest request) {
        Game game = findGameOrThrow(id);
        gameScreenshotRepository.deleteAllByGame_Id(id);

        List<String> imageUrls = request.imageUrls();
        List<GameScreenshot> saved = new ArrayList<>(imageUrls.size());
        for (int i = 0; i < imageUrls.size(); i++) {
            saved.add(gameScreenshotRepository.save(GameScreenshot.builder()
                    .game(game)
                    .imageUrl(imageUrls.get(i))
                    .displayOrder(i)
                    .build()));
        }
        contentChangedPublisher.gameChanged(game.getSlug());
        return saved.stream().map(GameScreenshotResponse::from).toList();
    }

    @Transactional
    public GameRatingResponse upsertRating(Long id, GameRatingRequest request) {
        Game game = findGameOrThrow(id);
        Set<GameContentDescriptor> descriptors = parseDescriptors(request.contentDescriptors());

        GameRating rating = gameRatingRepository.findByGame_Id(id)
                .orElseGet(() -> gameRatingRepository.save(
                        GameRating.builder().game(game).rating(request.rating()).build()));
        rating.update(request.rating(), request.classificationNumber(), request.classificationDate(),
                request.businessName(), request.developerReportNumber(), descriptors);
        contentChangedPublisher.gameChanged(game.getSlug());
        return GameRatingResponse.from(rating);
    }

    @Transactional
    public void deleteRating(Long id) {
        Game game = findGameOrThrow(id);
        gameRatingRepository.findByGame_Id(id).ifPresent(gameRatingRepository::delete);
        contentChangedPublisher.gameChanged(game.getSlug());
    }

    private Set<GameContentDescriptor> parseDescriptors(List<String> keys) {
        if (keys == null) {
            return EnumSet.noneOf(GameContentDescriptor.class);
        }
        Set<GameContentDescriptor> descriptors = EnumSet.noneOf(GameContentDescriptor.class);
        for (String key : keys) {
            try {
                descriptors.add(GameContentDescriptor.valueOf(key.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new GameException(GameExceptionType.INVALID_CONTENT_DESCRIPTOR);
            }
        }
        return descriptors;
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
