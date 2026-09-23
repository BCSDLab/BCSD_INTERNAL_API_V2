package com.bcsdlab.bcsdinternalapiv2.member.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MemberTest {

    @Test
    @DisplayName("ADMIN은 트랙에 관계없이 관리 권한을 가진다")
    void admin은_어떤_트랙이든_관리할_수_있다() {
        Member admin = member(MemberRole.ADMIN, MemberType.BEGINNER, track(1L));
        TrackMaster otherTrack = track(2L);

        assertThat(admin.canManage(otherTrack)).isTrue();
    }

    @Test
    @DisplayName("같은 트랙의 MENTOR는 관리 권한을 가진다")
    void 같은_트랙의_멘토는_관리할_수_있다() {
        TrackMaster track = track(1L);
        Member mentor = member(MemberRole.MEMBER, MemberType.MENTOR, track);

        assertThat(mentor.canManage(track)).isTrue();
    }

    @Test
    @DisplayName("같은 트랙의 REGULAR는 관리 권한을 가진다")
    void 같은_트랙의_정회원은_관리할_수_있다() {
        TrackMaster track = track(1L);
        Member regular = member(MemberRole.MEMBER, MemberType.REGULAR, track);

        assertThat(regular.canManage(track)).isTrue();
    }

    @Test
    @DisplayName("같은 트랙이어도 BEGINNER는 관리 권한이 없다")
    void 같은_트랙이어도_비기너는_관리할_수_없다() {
        TrackMaster track = track(1L);
        Member beginner = member(MemberRole.MEMBER, MemberType.BEGINNER, track);

        assertThat(beginner.canManage(track)).isFalse();
    }

    @Test
    @DisplayName("다른 트랙의 MENTOR는 관리 권한이 없다")
    void 다른_트랙의_멘토는_관리할_수_없다() {
        Member mentor = member(MemberRole.MEMBER, MemberType.MENTOR, track(1L));
        TrackMaster otherTrack = track(2L);

        assertThat(mentor.canManage(otherTrack)).isFalse();
    }

    @Test
    @DisplayName("트랙이 없는 부원은 관리 권한이 없다")
    void 트랙이_없으면_관리할_수_없다() {
        Member mentor = member(MemberRole.MEMBER, MemberType.MENTOR, null);
        TrackMaster otherTrack = track(1L);

        assertThat(mentor.canManage(otherTrack)).isFalse();
    }

    private Member member(MemberRole role, MemberType memberType, TrackMaster track) {
        Member member = Member.builder()
                .studentNumber("20240001")
                .password("password")
                .name("테스트")
                .track(track)
                .generation("1")
                .memberType(memberType)
                .university("대학")
                .department("학과")
                .email("test@bcsdlab.com")
                .role(role)
                .build();
        ReflectionTestUtils.setField(member, "id", 100L);
        return member;
    }

    private TrackMaster track(Long id) {
        TrackMaster track = TrackMaster.builder()
                .code("TRACK-" + id)
                .name("트랙 " + id)
                .build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }
}
