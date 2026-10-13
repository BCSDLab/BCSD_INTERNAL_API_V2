package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.model.PasswordResetToken;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.PasswordResetTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.global.util.TokenHasher;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackApiException;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackPostResult;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.ResourceAccessException;

@TestPropertySource(properties = "app.mail.transport=log")
class AdminPasswordLinkSlackIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";
    private static final String NEW_PASSWORD = "NewPass1234";
    private static final String SLACK_ID = "U0ABCDEFGH1";
    private static final Pattern TOKEN_IN_URL = Pattern.compile("\\?token=([A-Za-z0-9_-]+)");

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SlackClient slackClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster backend;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        saveMember("20230001", "관리자", MemberRole.ADMIN, MemberStatus.ACTIVE, null);
        adminToken = login("20230001", RAW_PASSWORD);
    }

    @Test
    void 링크를_DM으로_보내면_그_링크로_비밀번호를_설정하고_로그인할_수_있다() throws Exception {
        Member member = saveMember("20240001", "김하준", MemberRole.MEMBER, MemberStatus.ACTIVE, SLACK_ID);
        PasswordResetToken oldToken = saveUnusedToken(member.getId(), "old-email-token");

        sendLink(member.getId()).andExpect(status().isNoContent());

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(slackClient).postDirectMessage(eq(SLACK_ID), text.capture());
        assertThat(text.getValue()).contains("김하준님", "비밀번호 설정 링크", "24시간");
        String rawToken = extractToken(text.getValue());

        // 기존 미사용 토큰은 무효가 되고, 새 토큰은 24시간 유효하다.
        assertThat(passwordResetTokenRepository.findById(oldToken.getId()).orElseThrow().isUsed()).isTrue();
        PasswordResetToken issued = passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
                .orElseThrow();
        assertThat(issued.isUsed()).isFalse();
        assertThat(Duration.between(issued.getCreatedAt(), issued.getExpiresAt())).isEqualTo(Duration.ofHours(24));
        assertThat(issued.getMailSentAt()).as("DM으로 전달한 시각을 남긴다").isNotNull();

        validate(rawToken).andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.accountSetup").value(false));
        mockMvc.perform(post("/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"%s","newPasswordConfirm":"%s"}
                                """.formatted(rawToken, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isNoContent());

        login("20240001", NEW_PASSWORD);
        validate(rawToken).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용된 링크입니다."));
    }

    @Test
    void 초기_설정_전_회원에게는_계정_설정_문구로_보낸다() throws Exception {
        Member member = saveMember("20240002", "이서연", MemberRole.MEMBER, MemberStatus.PENDING_SETUP, SLACK_ID);

        sendLink(member.getId()).andExpect(status().isNoContent());

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(slackClient).postDirectMessage(eq(SLACK_ID), text.capture());
        assertThat(text.getValue()).contains("이서연님", "계정 설정 링크");
        validate(extractToken(text.getValue())).andExpect(jsonPath("$.accountSetup").value(true));

        // 링크로 비밀번호를 정해도 가입 전 상태는 그대로라, 로그인하면 초기 설정으로 이어진다.
        mockMvc.perform(post("/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"%s","newPasswordConfirm":"%s"}
                                """.formatted(extractToken(text.getValue()), NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isNoContent());
        String accessToken = login("20240002", NEW_PASSWORD);
        mockMvc.perform(get("/v1/members/me/initial-setup").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void Slack_ID가_없으면_400이고_토큰을_만들지_않는다() throws Exception {
        Member member = saveMember("20240003", "박지안", MemberRole.MEMBER, MemberStatus.ACTIVE, null);

        sendLink(member.getId()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Slack ID가 등록되지 않은 회원입니다."));

        assertThat(passwordResetTokenRepository.findAll()).isEmpty();
        verify(slackClient, never()).postDirectMessage(anyString(), anyString());
    }

    @Test
    void 탈퇴한_회원이면_409다() throws Exception {
        Member member = saveMember("20240004", "강태오", MemberRole.MEMBER, MemberStatus.WITHDRAWN, SLACK_ID);

        sendLink(member.getId()).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("탈퇴한 회원입니다."));

        assertThat(passwordResetTokenRepository.findAll()).isEmpty();
        verify(slackClient, never()).postDirectMessage(anyString(), anyString());
    }

    @Test
    void 없는_회원이면_404다() throws Exception {
        sendLink(999_999L).andExpect(status().isNotFound());
    }

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        Member member = saveMember("20240005", "일반", MemberRole.MEMBER, MemberStatus.ACTIVE, SLACK_ID);
        String memberToken = login("20240005", RAW_PASSWORD);

        mockMvc.perform(post("/v1/admin/members/{memberId}/password-link/slack", member.getId())
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());

        verify(slackClient, never()).postDirectMessage(anyString(), anyString());
    }

    @Test
    void DM_발송이_실패하면_502이고_발급한_링크는_쓸_수_없다() throws Exception {
        Member member = saveMember("20240006", "서지훈", MemberRole.MEMBER, MemberStatus.ACTIVE, SLACK_ID);
        doThrow(new SlackApiException("channel_not_found", null))
                .when(slackClient).postDirectMessage(anyString(), anyString());

        // Slack 오류 원문은 응답에 넣지 않고 관리자가 확인할 것을 알려 준다.
        sendLink(member.getId()).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value(
                        "Slack에서 이 회원에게 DM을 보낼 수 없습니다. Slack ID가 맞는지, 워크스페이스를 떠난 계정은 아닌지 확인하세요."))
                .andExpect(jsonPath("$.message").value(not(containsString("slackError"))));

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(slackClient).postDirectMessage(eq(SLACK_ID), text.capture());
        validate(extractToken(text.getValue())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 사용된 링크입니다."));
        assertThat(passwordResetTokenRepository.findAll()).allMatch(PasswordResetToken::isUsed);
    }

    @Test
    void Slack_연결_오류도_502이고_발급한_링크는_쓸_수_없다() throws Exception {
        Member member = saveMember("20240007", "한소미", MemberRole.MEMBER, MemberStatus.ACTIVE, SLACK_ID);
        doThrow(new ResourceAccessException("timeout"))
                .when(slackClient).postDirectMessage(anyString(), anyString());

        sendLink(member.getId()).andExpect(status().isBadGateway());

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
        assertThat(tokens).hasSize(1).allMatch(PasswordResetToken::isUsed);
    }

    @Test
    void 보내지_못했으면_1분을_기다리지_않고_바로_다시_보낼_수_있다() throws Exception {
        Member member = saveMember("20240009", "윤도현", MemberRole.MEMBER, MemberStatus.ACTIVE, SLACK_ID);
        doThrow(new SlackApiException("ratelimited", 30L))
                .doReturn(new SlackPostResult("D0DMCHANNEL", "1.0"))
                .when(slackClient).postDirectMessage(anyString(), anyString());

        sendLink(member.getId()).andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Slack 요청이 많아 보내지 못했습니다. 잠시 후 다시 시도하세요."));
        sendLink(member.getId()).andExpect(status().isNoContent());
    }

    @Test
    void 같은_회원에게_1분_안에_다시_보내면_429다() throws Exception {
        Member member = saveMember("20240008", "정한결", MemberRole.MEMBER, MemberStatus.ACTIVE, SLACK_ID);

        sendLink(member.getId()).andExpect(status().isNoContent());
        sendLink(member.getId()).andExpect(status().isTooManyRequests());

        verify(slackClient).postDirectMessage(eq(SLACK_ID), anyString());
    }

    private ResultActions sendLink(Long memberId) throws Exception {
        return mockMvc.perform(post("/v1/admin/members/{memberId}/password-link/slack", memberId)
                .header("Authorization", "Bearer " + adminToken));
    }

    private ResultActions validate(String rawToken) throws Exception {
        return mockMvc.perform(post("/v1/auth/password/reset-requests/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token":"%s"}
                        """.formatted(rawToken)));
    }

    private static String extractToken(String text) {
        Matcher matcher = TOKEN_IN_URL.matcher(text);
        assertThat(matcher.find()).as("DM 본문에 토큰이 담긴 링크가 있어야 한다").isTrue();
        return matcher.group(1);
    }

    private PasswordResetToken saveUnusedToken(Long memberId, String rawToken) {
        Instant now = Instant.now();
        return passwordResetTokenRepository.save(PasswordResetToken.builder()
                .memberId(memberId)
                .tokenHash(TokenHasher.sha256Hex(rawToken))
                .expiresAt(now.plus(Duration.ofMinutes(30)))
                .createdAt(now)
                .build());
    }

    private Member saveMember(String studentNumber, String name, MemberRole role, MemberStatus memberStatus,
                              String slackId) {
        Member member = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name(name)
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email(studentNumber + "@bcsd.club")
                .status(memberStatus)
                .role(role)
                .build();
        member.updateSlackId(slackId);
        return memberRepository.save(member);
    }

    private String login(String studentNumber, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentNumber":"%s","password":"%s","rememberMe":false}
                                """.formatted(studentNumber, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }
}
