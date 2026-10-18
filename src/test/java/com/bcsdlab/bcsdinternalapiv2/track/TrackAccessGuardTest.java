package com.bcsdlab.bcsdinternalapiv2.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bcsdlab.bcsdinternalapiv2.game.exception.GameException;
import com.bcsdlab.bcsdinternalapiv2.game.exception.GameExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.service.TrackAccessGuard;
import com.bcsdlab.bcsdinternalapiv2.track.service.TrackAccessGuard.AccessErrors;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrackAccessGuardTest {

    private static final AccessErrors ERRORS = new AccessErrors(
            GameException::new, GameExceptionType.MEMBER_NOT_FOUND, GameExceptionType.GAME_ACCESS_DENIED);

    private final MemberRepository memberRepository = mock(MemberRepository.class);
    private final TrackMasterRepository trackMasterRepository = mock(TrackMasterRepository.class);
    private final TrackAccessGuard guard = new TrackAccessGuard(memberRepository, trackMasterRepository);

    @Test
    void 회원이_없으면_회원_없음_예외다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guard.requireManager(1L, null, ERRORS))
                .isInstanceOf(GameException.class)
                .satisfies(e -> assertExceptionType(e, GameExceptionType.MEMBER_NOT_FOUND));
    }

    @Test
    void GAME_트랙_행이_없으면_관리자만_통과한다() {
        when(trackMasterRepository.findByCode("GAME")).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(MemberRole.ADMIN)));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member(MemberRole.MEMBER)));

        assertThatCode(() -> guard.requireGameManager(1L, ERRORS)).doesNotThrowAnyException();
        assertThatThrownBy(() -> guard.requireGameManager(2L, ERRORS))
                .isInstanceOf(GameException.class)
                .satisfies(e -> assertExceptionType(e, GameExceptionType.GAME_ACCESS_DENIED));
    }

    private static Member member(MemberRole role) {
        return Member.builder().role(role).memberType(MemberType.MENTOR).build();
    }

    private static void assertExceptionType(Throwable e, GameExceptionType expected) {
        assertThat(((GameException) e).getExceptionType()).isEqualTo(expected);
    }
}
