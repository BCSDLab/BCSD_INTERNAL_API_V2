package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdException;
import com.bcsdlab.bcsdinternalapiv2.global.exception.BcsdExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.member.repository.TrackMasterRepository;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 트랙 콘텐츠(트랙 페이지·커리큘럼·게임)를 고칠 수 있는지 확인한다. 관리자, 또는 해당 트랙의 멘토·정회원만
 * 통과한다({@link Member#canManage}). 도메인마다 응답 메시지가 달라서 던질 예외는 호출하는 쪽이 정한다.
 */
@Component
@RequiredArgsConstructor
public class TrackAccessGuard {

    private final MemberRepository memberRepository;
    private final TrackMasterRepository trackMasterRepository;

    public void requireManager(Long memberId, TrackMaster track, AccessErrors errors) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> errors.exception().apply(errors.memberNotFound()));
        if (!member.canManage(track)) {
            throw errors.exception().apply(errors.accessDenied());
        }
    }

    /** 게임은 GAME 트랙 소관이다. GAME 트랙 행이 없으면 관리자만 통과한다. */
    public void requireGameManager(Long memberId, AccessErrors errors) {
        TrackMaster gameTrack = trackMasterRepository.findByCode(Track.GAME.name()).orElse(null);
        requireManager(memberId, gameTrack, errors);
    }

    /** 도메인별 예외 생성자와 "회원 없음"·"권한 없음" 예외 타입. */
    public record AccessErrors(Function<BcsdExceptionType, ? extends BcsdException> exception,
                               BcsdExceptionType memberNotFound,
                               BcsdExceptionType accessDenied) {
    }
}
