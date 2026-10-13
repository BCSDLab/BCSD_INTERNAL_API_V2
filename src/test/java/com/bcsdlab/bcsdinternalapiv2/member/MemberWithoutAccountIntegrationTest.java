package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.PasswordResetTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.global.mail.MailSender;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

/** 로그인 계정 없이 명부에만 등록한 회원(졸업생·멘토 등)의 생성·로그인·재설정·초대 흐름과 직장 필드. */
class MemberWithoutAccountIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "AdminPass1";
    private static final String NEW_PASSWORD = "NewPass1234";
    private static final String SLACK_ID = "U0ABCDEFGH1";
    private static final Pattern TOKEN_IN_URL = Pattern.compile("\\?token=([A-Za-z0-9_-]+)");
    // 셀프 재설정 요청은 IP마다 3초 제한이 있다. 요청마다 다른 IP를 써서 테스트끼리 간섭하지 않게 한다.
    private static final AtomicInteger IP_SEQUENCE = new AtomicInteger(1);

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

    // 부원 추가가 이메일로 Slack ID를 조회한다 — 실제 slack.com을 부르지 않게 막는다.
    @MockitoBean
    private SlackClient slackClient;

    @MockitoBean
    private MailSender mailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster backend;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        Member admin = Member.builder()
                .studentNumber("20230001")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("관리자")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("admin@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .passwordChangedAt(Instant.now())
                .build();
        memberRepository.save(admin);
        adminToken = loginToken("20230001", RAW_PASSWORD);
    }

    @Test
    void 계정_없이_등록하면_비밀번호가_없고_환영_메일을_보내지_않으며_직장이_저장된다() throws Exception {
        long memberId = createMember("20150001", "alumni@bcsd.club", false, "  비씨에스디랩  ");

        Member saved = memberRepository.findById(memberId).orElseThrow();
        assertThat(saved.getPassword()).isNull();
        assertThat(saved.hasAccount()).isFalse();
        assertThat(saved.getPasswordChangedAt()).isNull();
        assertThat(saved.getStatus()).isEqualTo(MemberStatus.PENDING_SETUP);
        assertThat(saved.getCompany()).isEqualTo("비씨에스디랩");

        verify(mailSender, after(500).never()).sendAccountCreated(anyString(), anyString(), anyString(), anyString());
        assertThat(memberRepository.findById(memberId).orElseThrow().getWelcomeMailSentAt()).isNull();
        // 생성 직후 이메일로 Slack ID를 찾는 동작은 계정 유무와 관계없이 그대로다.
        verify(slackClient).findSlackIdByEmail("alumni@bcsd.club");

        mockMvc.perform(get("/v1/admin/members").param("keyword", "20150001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].company").value("비씨에스디랩"))
                .andExpect(jsonPath("$.members[0].hasAccount").value(false));
    }

    @Test
    void createAccount를_생략하면_기존처럼_계정을_만들고_환영_메일을_보낸다() throws Exception {
        long memberId = createMember("20240001", "new-member@bcsd.club", null, null);

        verify(mailSender, timeout(5000)).sendAccountCreated(eq("new-member@bcsd.club"), eq("20240001"),
                anyString(), anyString());
        Member saved = memberRepository.findById(memberId).orElseThrow();
        assertThat(saved.hasAccount()).isTrue();
        assertThat(saved.getCompany()).isNull();

        mockMvc.perform(get("/v1/admin/members").param("keyword", "20240001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].company").isEmpty())
                .andExpect(jsonPath("$.members[0].hasAccount").value(true));
    }

    @Test
    void 계정_없는_회원으로_로그인하면_401_INVALID_CREDENTIALS이고_실패_횟수가_쌓인다() throws Exception {
        long memberId = createMember("20150002", "alumni2@bcsd.club", false, null);

        login("20150002", "anything-1234")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("학번 또는 비밀번호가 올바르지 않습니다."));

        assertThat(memberRepository.findById(memberId).orElseThrow().getLoginFailCount()).isEqualTo((short) 1);
    }

    @Test
    void 계정_없는_회원의_이메일로_셀프_재설정을_요청하면_없는_이메일과_같은_응답이고_토큰과_메일이_없다() throws Exception {
        createMember("20150003", "alumni3@bcsd.club", false, null);

        String accountlessBody = requestReset("alumni3@bcsd.club")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String unknownBody = requestReset("nobody@bcsd.club")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(accountlessBody).isEqualTo(unknownBody);
        assertThat(passwordResetTokenRepository.findAll()).isEmpty();
        verify(mailSender, after(500).never()).sendPasswordResetLink(anyString(), anyString());

        // 요청 제한은 계정 유무와 관계없이 똑같이 걸린다.
        requestReset("alumni3@bcsd.club").andExpect(status().isTooManyRequests());
    }

    @Test
    void 계정_없는_회원에게_환영_메일을_재발송하면_임시_비밀번호로_로그인해_초기_설정으로_이어진다() throws Exception {
        long memberId = createMember("20150004", "alumni4@bcsd.club", false, null);

        mockMvc.perform(post("/v1/admin/members/{memberId}/resend-welcome-mail", memberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        ArgumentCaptor<String> temporaryPassword = ArgumentCaptor.forClass(String.class);
        verify(mailSender, timeout(5000)).sendAccountCreated(eq("alumni4@bcsd.club"), eq("20150004"),
                temporaryPassword.capture(), anyString());

        Member invited = memberRepository.findById(memberId).orElseThrow();
        assertThat(invited.hasAccount()).isTrue();
        assertThat(invited.getStatus()).isEqualTo(MemberStatus.PENDING_SETUP);

        String responseBody = login("20150004", temporaryPassword.getValue())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_SETUP"))
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();
        mockMvc.perform(get("/v1/members/me/initial-setup").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void 계정_없는_회원에게_Slack_링크를_보내면_비밀번호를_정하고_로그인할_수_있다() throws Exception {
        long memberId = createMember("20150005", "alumni5@bcsd.club", false, null);
        memberRepository.findById(memberId).ifPresent(member -> {
            member.updateSlackId(SLACK_ID);
            memberRepository.save(member);
        });

        mockMvc.perform(post("/v1/admin/members/{memberId}/password-link/slack", memberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(slackClient).postDirectMessage(eq(SLACK_ID), text.capture());
        assertThat(text.getValue()).contains("계정 설정 링크");

        mockMvc.perform(post("/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"%s","newPasswordConfirm":"%s"}
                                """.formatted(extractToken(text.getValue()), NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isNoContent());

        assertThat(memberRepository.findById(memberId).orElseThrow().hasAccount()).isTrue();
        String responseBody = login("20150005", NEW_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_SETUP"))
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();
        mockMvc.perform(get("/v1/members/me/initial-setup").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void 프로필_수정으로_직장을_설정하고_비울_수_있다() throws Exception {
        long memberId = createMember("20150006", "alumni6@bcsd.club", false, null);

        updateProfile(memberId, "\"코인\"").andExpect(status().isNoContent());
        assertThat(memberRepository.findById(memberId).orElseThrow().getCompany()).isEqualTo("코인");

        updateProfile(memberId, "\"   \"").andExpect(status().isNoContent());
        assertThat(memberRepository.findById(memberId).orElseThrow().getCompany()).isNull();

        updateProfile(memberId, "\"" + "가".repeat(101) + "\"").andExpect(status().isBadRequest());
    }

    @Test
    void 일반_인명부에는_직장과_계정_유무가_나가지_않는다() throws Exception {
        createMember("20150007", "alumni7@bcsd.club", false, "비씨에스디랩");
        Member member = Member.builder()
                .studentNumber("20240002")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("일반")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("member@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .passwordChangedAt(Instant.now())
                .build();
        memberRepository.save(member);
        String memberToken = loginToken("20240002", RAW_PASSWORD);

        mockMvc.perform(get("/v1/members/directory").param("keyword", "20150007")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].studentNumber").value("20150007"))
                .andExpect(jsonPath("$.members[0].company").doesNotExist())
                .andExpect(jsonPath("$.members[0].hasAccount").doesNotExist());
    }

    private long createMember(String studentNumber, String email, Boolean createAccount, String company)
            throws Exception {
        StringBuilder body = new StringBuilder("""
                {"name":"졸업생","studentNumber":"%s","track":"BACKEND","memberType":"MENTOR",
                 "generation":"15-상","university":"OO대학교","department":"컴퓨터공학과","email":"%s"
                """.formatted(studentNumber, email));
        if (createAccount != null) {
            body.append(",\"createAccount\":").append(createAccount);
        }
        if (company != null) {
            body.append(",\"company\":\"").append(company).append('"');
        }
        body.append('}');

        String responseBody = mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    private ResultActions updateProfile(Long memberId, String companyJson) throws Exception {
        return mockMvc.perform(patch("/v1/admin/members/{memberId}", memberId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"졸업생","track":"BACKEND","memberType":"MENTOR","generation":"15-상",
                         "university":"OO대학교","department":"컴퓨터공학과","positionCodes":[],
                         "duesRequired":false,"email":"alumni6@bcsd.club","company":%s}
                        """.formatted(companyJson)));
    }

    private ResultActions requestReset(String email) throws Exception {
        String ip = "10.0.0." + IP_SEQUENCE.getAndIncrement();
        return mockMvc.perform(post("/v1/auth/password/reset-requests")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s"}
                        """.formatted(email)));
    }

    private ResultActions login(String studentNumber, String password) throws Exception {
        return mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"studentNumber":"%s","password":"%s","rememberMe":false}
                        """.formatted(studentNumber, password)));
    }

    private String loginToken(String studentNumber, String password) throws Exception {
        String responseBody = login(studentNumber, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    private static String extractToken(String text) {
        Matcher matcher = TOKEN_IN_URL.matcher(text);
        assertThat(matcher.find()).as("DM 본문에 토큰이 담긴 링크가 있어야 한다").isTrue();
        return matcher.group(1);
    }
}
