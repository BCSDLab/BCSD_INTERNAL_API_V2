package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameBuildResponse;
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

    private final GameRepository gameRepository;
    private final GameBuildRepository gameBuildRepository;
    private final MemberRepository memberRepository;

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
    public void deleteBuild(Long gameId, Long buildId) {
        findGameOrThrow(gameId);
        GameBuild build = gameBuildRepository.findById(buildId)
                .filter(b -> b.getGame().getId().equals(gameId))
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_BUILD_NOT_FOUND));
        gameBuildRepository.delete(build);
    }

    private Game findGameOrThrow(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_NOT_FOUND));
    }
}
