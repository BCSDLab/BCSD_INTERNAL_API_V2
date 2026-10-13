package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackProfileSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberDirectoryService;
import com.bcsdlab.bcsdinternalapiv2.member.service.SlackProfileSyncService;
import java.util.List;
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
