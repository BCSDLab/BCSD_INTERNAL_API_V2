package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameActiveBuildResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameRatingResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.GameSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.game.config.GameBuildProperties;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameBuildStatus;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameMember;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameScreenshot;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameBuildRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRatingRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameScreenshotRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;
    private final GameScreenshotRepository gameScreenshotRepository;
    private final GameRatingRepository gameRatingRepository;
    private final GameBuildRepository gameBuildRepository;
    private final GameMemberRepository gameMemberRepository;
    private final MemberRepository memberRepository;
    private final GameBuildProperties gameBuildProperties;

    public List<GameSummaryResponse> getGames() {
        return gameRepository.findAllByPublishedTrueOrderByDisplayOrderAsc().stream()
                .map(GameSummaryResponse::from)
                .toList();
    }

    public GameDetailResponse getGame(String slug) {
        Game game = gameRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_NOT_FOUND));

        List<String> screenshots = gameScreenshotRepository
                .findAllByGame_IdOrderByDisplayOrderAsc(game.getId()).stream()
                .map(GameScreenshot::getImageUrl)
                .toList();

        GameRatingResponse rating = gameRatingRepository.findByGame_Id(game.getId())
                .map(GameRatingResponse::from)
                .orElse(null);

        GameActiveBuildResponse activeBuild = gameBuildRepository
                .findFirstByGame_IdAndStatusOrderByUploadedAtDesc(game.getId(), GameBuildStatus.ACTIVE)
                .map(build -> GameActiveBuildResponse.from(build, gameBuildProperties.publicOrigin()))
                .orElse(null);

        List<GameMemberResponse> members = publishedMembers(game.getId());

        return GameDetailResponse.of(game, screenshots, members, rating, activeBuild);
    }

    /**
     * 트랙 공개 조회(TrackService)와 같은 이유로 N+1을 피한다 —
     * 배정 목록 1쿼리 + 부원 IN절 배치 조회 1쿼리로 끝낸다.
     */
    private List<GameMemberResponse> publishedMembers(Long gameId) {
        List<GameMember> assignments = gameMemberRepository.findAllByGame_IdOrderByDisplayOrderAsc(gameId);
        List<Long> memberIds = assignments.stream().map(a -> a.getMember().getId()).toList();
        Map<Long, Member> membersById = memberIds.isEmpty()
                ? Map.of()
                : memberRepository.findAllById(memberIds).stream()
                        .collect(Collectors.toMap(Member::getId, member -> member));

        return assignments.stream()
                .map(a -> membersById.get(a.getMember().getId()))
                .filter(member -> member != null && member.isActive())
                .map(GameMemberResponse::from)
                .toList();
    }
}
