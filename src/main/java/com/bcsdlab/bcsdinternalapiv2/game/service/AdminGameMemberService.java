package com.bcsdlab.bcsdinternalapiv2.game.service;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameMembersAttachRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.game.model.Game;
import com.bcsdlab.bcsdinternalapiv2.game.model.GameMember;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.game.repository.GameRepository;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임에 참여 멤버를 배정한다(ADR-012 패턴 재사용). 명부는 이미 구현되어 있으므로 포트
 * 추상화 없이 {@code MemberRepository}를 직접 조회한다. 이름·등급·프로필 사진에 대한
 * 쓰기 경로는 만들지 않는다(INV-19) — 이 서비스는 배정·순서만 다룬다(멘토와 달리 게임
 * 참여 멤버는 별도 숨김 토글이 없다 — 와이어프레임 1b 참고).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGameMemberService {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final MemberRepository memberRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public List<AdminGameMemberResponse> getMembers(Long gameId) {
        findGameOrThrow(gameId);
        return gameMemberRepository.findAllByGame_IdOrderByDisplayOrderAsc(gameId).stream()
                .map(AdminGameMemberResponse::from)
                .toList();
    }

    @Transactional
    public List<AdminGameMemberResponse> attachMembers(Long gameId, GameMembersAttachRequest request) {
        Game game = findGameOrThrow(gameId);

        List<Long> memberIds = request.memberIds();
        if (new HashSet<>(memberIds).size() != memberIds.size()) {
            throw new GameException(GameExceptionType.MEMBER_ALREADY_ASSIGNED);
        }
        Map<Long, Member> byId = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));
        if (byId.size() != memberIds.size()) {
            throw new GameException(GameExceptionType.MEMBER_NOT_FOUND);
        }
        for (Long memberId : memberIds) {
            if (gameMemberRepository.existsByGame_IdAndMember_Id(gameId, memberId)) {
                throw new GameException(GameExceptionType.MEMBER_ALREADY_ASSIGNED);
            }
        }

        int nextOrder = gameMemberRepository.findAllByGame_IdOrderByDisplayOrderAsc(gameId).size();
        for (Long memberId : memberIds) {
            gameMemberRepository.save(GameMember.builder()
                    .game(game)
                    .member(byId.get(memberId))
                    .displayOrder(nextOrder++)
                    .build());
        }
        contentChangedPublisher.gameChanged(game.getSlug());
        return getMembers(gameId);
    }

    @Transactional
    public void detachMember(Long gameId, Long memberId) {
        Game game = findGameOrThrow(gameId);
        GameMember assignment = gameMemberRepository.findByGame_IdAndMember_Id(gameId, memberId)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_MEMBER_NOT_FOUND));
        gameMemberRepository.delete(assignment);
        contentChangedPublisher.gameChanged(game.getSlug());
    }

    @Transactional
    public void reorder(Long gameId, OrderRequest request) {
        Game game = findGameOrThrow(gameId);
        List<GameMember> assignments = gameMemberRepository.findAllByGame_IdOrderByDisplayOrderAsc(gameId);
        Map<Long, GameMember> byId = assignments.stream()
                .collect(Collectors.toMap(GameMember::getId, assignment -> assignment));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
        contentChangedPublisher.gameChanged(game.getSlug());
    }

    private Game findGameOrThrow(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameException(GameExceptionType.GAME_NOT_FOUND));
    }
}
