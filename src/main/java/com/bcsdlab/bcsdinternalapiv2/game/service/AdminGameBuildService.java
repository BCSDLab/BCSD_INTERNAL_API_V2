package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameBuildResponse;
import com.bcsdlab.bcsdinternalapiv2.game.config.GameBuildProperties;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameBuildUploadTokenResponse;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuild;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameBuildRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임 빌드 메타데이터 등록(T-39). 이번 1차는 버전·상태만 다룬다 — 실제 ZIP
 * 업로드·압축해제·서빙은 후속 태스크(T-41, ADR-023)에서 홈페이지 서버 쪽 설계와
 * 함께 구현한다. 여기서 만드는 빌드는 항상 {@code status=PENDING}이고
 * {@code buildFileUrl}은 null이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGameBuildService {

    private static final Set<GameBuildStatus> TOKEN_ISSUABLE_STATUSES = Set.of(
            GameBuildStatus.PENDING, GameBuildStatus.FAILED);

    private final GameRepository gameRepository;
    private final GameBuildRepository gameBuildRepository;
    private final MemberRepository memberRepository;
    private final GameBuildProperties gameBuildProperties;
    private final GameBuildTokenService gameBuildTokenService;

    public List<AdminGameBuildResponse> getBuilds(Long gameId) {
        findGameOrThrow(gameId);
        return gameBuildRepository.findAllByGame_IdOrderByUploadedAtDesc(gameId).stream()
                .map(AdminGameBuildResponse::from)
                .toList();
    }

    @Transactional
    public AdminGameBuildResponse createBuild(Long gameId, GameBuildCreateRequest request, Long uploadedByMemberId) {
        Game game = findGameOrThrow(gameId);
        Member uploadedBy = memberRepository.findById(uploadedByMemberId).orElse(null);

        GameBuild build = gameBuildRepository.save(GameBuild.builder()
                .game(game)
                .version(request.version())
                .status(GameBuildStatus.PENDING)
                .uploadedBy(uploadedBy)
                .uploadedAt(Instant.now())
                .build());
        return AdminGameBuildResponse.from(build);
    }

    @Transactional
    public GameBuildUploadTokenResponse issueUploadToken(Long gameId, Long buildId) {
        Game game = findGameOrThrow(gameId);
        GameBuild build = findBuildOrThrow(gameId, buildId);
        if (!TOKEN_ISSUABLE_STATUSES.contains(build.getStatus())) {
            throw new GameException(GameExceptionType.GAME_BUILD_INVALID_STATE);
        }

        GameBuildTokenService.IssuedToken issued = gameBuildTokenService.issue(
                build.getId(), game.getId(), game.getSlug(), build.getVersion());
        build.updateStatus(GameBuildStatus.PROCESSING);
        return new GameBuildUploadTokenResponse(gameBuildProperties.uploadUrl(), issued.token(), issued.expiresAt());
    }

    @Transactional
    public void deleteBuild(Long gameId, Long buildId) {
        findGameOrThrow(gameId);
        GameBuild build = findBuildOrThrow(gameId, buildId);
        gameBuildRepository.delete(build);
    }

    private Game findGameOrThrow(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_NOT_FOUND));
    }

    private GameBuild findBuildOrThrow(Long gameId, Long buildId) {
        return gameBuildRepository.findById(buildId)
                .filter(b -> b.getGame().getId().equals(gameId))
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_BUILD_NOT_FOUND));
    }
}
