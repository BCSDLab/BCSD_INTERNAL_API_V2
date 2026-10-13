package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackProfileSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberDirectoryService;
import com.bcsdlab.bcsdinternalapiv2.member.service.SlackProfileSyncService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SlackProfileSyncServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SlackClient slackClient;

    @Mock
    private MemberDirectoryService memberDirectoryService;

    @InjectMocks
    private SlackProfileSyncService slackProfileSyncService;

    @Test
    @DisplayName("탈퇴하지 않은 회원(가입 전·잠김 포함)을 대상으로 조회하고, 성공한 회원만 사진을 저장한다")
    void 탈퇴하지_않은_회원을_대상으로_동기화한다() {
        Member pending = member(1L, "pending@bcsd.club", MemberStatus.PENDING_SETUP);
        Member notInSlack = member(2L, "none@bcsd.club", MemberStatus.LOCKED);
        Member slackError = member(3L, "error@bcsd.club", MemberStatus.ACTIVE);

        when(memberRepository.findAllByStatusNot(MemberStatus.WITHDRAWN))
                .thenReturn(List.of(pending, notInSlack, slackError));
        when(slackClient.findProfileImageUrlByEmail("pending@bcsd.club")).thenReturn(Optional.of("https://img/p.png"));
        when(slackClient.findProfileImageUrlByEmail("none@bcsd.club")).thenReturn(Optional.empty());
        when(slackClient.findProfileImageUrlByEmail("error@bcsd.club")).thenThrow(new IllegalStateException("slack down"));

        SlackProfileSyncResponse result = slackProfileSyncService.syncAll();

        assertThat(result.total()).isEqualTo(3);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(2);
        verify(memberDirectoryService).updatePhotoUrl(1L, "https://img/p.png");
        // 사진을 못 찾았거나 조회가 실패한 회원은 저장하지 않는다 — 전체 호출은 1번뿐이다.
        verify(memberDirectoryService, times(1)).updatePhotoUrl(anyLong(), anyString());
    }

    @Test
    @DisplayName("Slack ID가 있는 회원은 users.list 결과로 매칭하고 회원별 이메일 조회를 하지 않는다")
    void Slack_ID가_있으면_목록으로_매칭한다() {
        Member relay = member(1L, "abc123@privaterelay.appleid.com", MemberStatus.ACTIVE, "U0RELAY0001");
        Member notInList = member(2L, "gone@bcsd.club", MemberStatus.ACTIVE, "U0NOTINLIST");

        when(memberRepository.findAllByStatusNot(MemberStatus.WITHDRAWN)).thenReturn(List.of(relay, notInList));
        when(slackClient.findAllProfileImageUrlsBySlackId())
                .thenReturn(Map.of("U0RELAY0001", "https://img/relay.png", "U0SOMEONE01", "https://img/other.png"));

        SlackProfileSyncResponse result = slackProfileSyncService.syncAll();

        assertThat(result.total()).isEqualTo(2);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        verify(memberDirectoryService).updatePhotoUrl(1L, "https://img/relay.png");
        verify(memberDirectoryService, times(1)).updatePhotoUrl(anyLong(), anyString());
        verify(slackClient, times(1)).findAllProfileImageUrlsBySlackId();
        verify(slackClient, never()).findProfileImageUrlByEmail(anyString());
    }

    @Test
    @DisplayName("Slack ID가 없는 회원만 이메일 조회로 보완한다")
    void Slack_ID가_없는_회원은_이메일로_조회한다() {
        Member withSlackId = member(1L, "a@bcsd.club", MemberStatus.ACTIVE, "U0WITHID001");
        Member withoutSlackId = member(2L, "b@bcsd.club", MemberStatus.ACTIVE, null);

        when(memberRepository.findAllByStatusNot(MemberStatus.WITHDRAWN)).thenReturn(List.of(withSlackId, withoutSlackId));
        when(slackClient.findAllProfileImageUrlsBySlackId()).thenReturn(Map.of("U0WITHID001", "https://img/a.png"));
        when(slackClient.findProfileImageUrlByEmail("b@bcsd.club")).thenReturn(Optional.of("https://img/b.png"));

        SlackProfileSyncResponse result = slackProfileSyncService.syncAll();

        assertThat(result.updated()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        verify(memberDirectoryService).updatePhotoUrl(1L, "https://img/a.png");
        verify(memberDirectoryService).updatePhotoUrl(2L, "https://img/b.png");
        verify(slackClient, never()).findProfileImageUrlByEmail("a@bcsd.club");
    }

    @Test
    @DisplayName("Slack ID가 있는 회원이 없으면 users.list를 부르지 않는다")
    void Slack_ID가_있는_회원이_없으면_목록을_부르지_않는다() {
        Member member = member(1L, "a@bcsd.club", MemberStatus.ACTIVE, null);
        when(memberRepository.findAllByStatusNot(MemberStatus.WITHDRAWN)).thenReturn(List.of(member));
        when(slackClient.findProfileImageUrlByEmail("a@bcsd.club")).thenReturn(Optional.of("https://img/a.png"));

        assertThat(slackProfileSyncService.syncAll().updated()).isEqualTo(1);
        verify(slackClient, never()).findAllProfileImageUrlsBySlackId();
    }

    @Test
    @DisplayName("users.list가 실패하면(429 포함) 아무도 갱신하지 않고 SLACK_SYNC_FAILED로 전체 실패한다")
    void 목록_조회가_실패하면_전체가_실패한다() {
        Member withSlackId = member(1L, "a@bcsd.club", MemberStatus.ACTIVE, "U0WITHID001");
        Member withoutSlackId = member(2L, "b@bcsd.club", MemberStatus.ACTIVE, null);
        when(memberRepository.findAllByStatusNot(MemberStatus.WITHDRAWN)).thenReturn(List.of(withSlackId, withoutSlackId));
        when(slackClient.findAllProfileImageUrlsBySlackId()).thenThrow(new SlackApiException("ratelimited", 30L));

        assertThatThrownBy(() -> slackProfileSyncService.syncAll())
                .isInstanceOf(MemberException.class)
                .satisfies(e -> assertThat(((MemberException) e).getExceptionType().getMessage())
                        .contains("slackError=ratelimited", "retryAfterSeconds=30"));

        verify(slackClient, never()).findProfileImageUrlByEmail(anyString());
        verify(memberDirectoryService, never()).updatePhotoUrl(anyLong(), anyString());
    }

    private Member member(long id, String email, MemberStatus status, String slackId) {
        Member member = member(id, email, status);
        member.updateSlackId(slackId);
        return member;
    }

    private Member member(long id, String email, MemberStatus status) {
        Member member = Member.builder()
                .studentNumber("2024%04d".formatted(id))
                .password("{noop}password")
                .name("테스트" + id)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email(email)
                .status(status)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
