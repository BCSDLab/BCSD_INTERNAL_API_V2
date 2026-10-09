package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = "app.mail.transport=log")
class MemberSlackIdIntegrationTest extends IntegrationTestSupport {

    private static final String RAW_PASSWORD = "Temp1234";
    private static final String SLACK_ID = "U0ABCDEFGH1";
    private static final String OTHER_SLACK_ID = "U0ZZZZZZZZ9";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        saveMember("20230001", "관리자", MemberRole.ADMIN);
        adminToken = login("20230001");
    }

    // ---------- PATCH /{memberId}/slack-id ----------

    @Test
    void Slack_ID를_저장하면_인명부_응답에_포함된다() throws Exception {
        Member member = saveMember("20240001", "김도윤", MemberRole.MEMBER);

        patchSlackId(member.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/admin/members").header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "20240001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].slackId").value(SLACK_ID));
    }

    @Test
    void 일반_회원_인명부에도_Slack_ID가_포함된다() throws Exception {
        Member member = saveMember("20240002", "이서연", MemberRole.MEMBER);
        patchSlackId(member.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());
        String memberToken = login("20240002");

        mockMvc.perform(get("/v1/members/directory").header("Authorization", "Bearer " + memberToken)
                        .param("keyword", "20240002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].slackId").value(SLACK_ID));
    }

    @Test
    void null을_보내면_Slack_ID가_지워진다() throws Exception {
        Member member = saveMember("20240003", "박지안", MemberRole.MEMBER);
        patchSlackId(member.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());

        patchSlackId(member.getId(), "null").andExpect(status().isNoContent());

        assertThat(memberRepository.findById(member.getId()).orElseThrow().getSlackId()).isNull();
    }

    @Test
    void 형식이_틀리면_400이다() throws Exception {
        Member member = saveMember("20240004", "강태오", MemberRole.MEMBER);

        patchSlackId(member.getId(), "\"u0abcdefgh1\"").andExpect(status().isBadRequest());
        patchSlackId(member.getId(), "\"U0ABC\"").andExpect(status().isBadRequest());
        patchSlackId(member.getId(), "\"U0ABC-EFGH1\"").andExpect(status().isBadRequest());
    }

    @Test
    void 다른_회원이_쓰는_Slack_ID면_409이고_그_회원_이름을_알려준다() throws Exception {
        Member owner = saveMember("20240005", "한소미", MemberRole.MEMBER);
        Member other = saveMember("20240006", "정한결", MemberRole.MEMBER);
        patchSlackId(owner.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());

        patchSlackId(other.getId(), "\"" + SLACK_ID + "\"")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("한소미")));

        assertThat(memberRepository.findById(other.getId()).orElseThrow().getSlackId()).isNull();
    }

    @Test
    void 자기_자신의_값으로_다시_저장하면_중복이_아니다() throws Exception {
        Member member = saveMember("20240007", "서지훈", MemberRole.MEMBER);
        patchSlackId(member.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());

        patchSlackId(member.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());
    }

    @Test
    void 없는_회원이면_404다() throws Exception {
        patchSlackId(999_999L, "\"" + SLACK_ID + "\"").andExpect(status().isNotFound());
    }

    @Test
    void 관리자가_아니면_403이다() throws Exception {
        Member member = saveMember("20240008", "일반", MemberRole.MEMBER);
        String memberToken = login("20240008");

        mockMvc.perform(patch("/v1/admin/members/{id}/slack-id", member.getId())
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slackId\":\"" + SLACK_ID + "\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- POST /slack-ids/lookup ----------

    @Test
    void 자동_조회는_회원별_결과를_돌려준다() throws Exception {
        Member found = saveMember("20240011", "찾음", MemberRole.MEMBER);
        Member notFound = saveMember("20240012", "없음", MemberRole.MEMBER);
        Member failed = saveMember("20240013", "실패", MemberRole.MEMBER);
        when(slackClient.findSlackIdByEmail(found.getEmail())).thenReturn(Optional.of(SLACK_ID));
        when(slackClient.findSlackIdByEmail(notFound.getEmail())).thenReturn(Optional.empty());
        when(slackClient.findSlackIdByEmail(failed.getEmail()))
                .thenThrow(new MemberException(MemberExceptionType.SLACK_SYNC_FAILED));

        lookup(found.getId(), notFound.getId(), failed.getId(), 999_999L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("SAVED"))
                .andExpect(jsonPath("$.results[0].slackId").value(SLACK_ID))
                .andExpect(jsonPath("$.results[1].status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.results[2].status").value("FAILED"))
                .andExpect(jsonPath("$.results[3].status").value("MEMBER_NOT_FOUND"));

        assertThat(memberRepository.findById(found.getId()).orElseThrow().getSlackId()).isEqualTo(SLACK_ID);
        assertThat(memberRepository.findById(notFound.getId()).orElseThrow().getSlackId()).isNull();
    }

    @Test
    void 자동_조회로_찾은_값을_다른_회원이_쓰면_저장하지_않고_DUPLICATED다() throws Exception {
        Member owner = saveMember("20240021", "주인", MemberRole.MEMBER);
        Member target = saveMember("20240022", "대상", MemberRole.MEMBER);
        patchSlackId(owner.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());
        when(slackClient.findSlackIdByEmail(target.getEmail())).thenReturn(Optional.of(SLACK_ID));

        lookup(target.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].status").value("DUPLICATED"))
                .andExpect(jsonPath("$.results[0].slackId").value(SLACK_ID))
                .andExpect(jsonPath("$.results[0].ownerName").value("주인"));

        assertThat(memberRepository.findById(target.getId()).orElseThrow().getSlackId()).isNull();
    }

    @Test
    void 이미_Slack_ID가_있는_회원은_Slack을_부르지_않는다() throws Exception {
        Member member = saveMember("20240031", "기존값", MemberRole.MEMBER);
        patchSlackId(member.getId(), "\"" + SLACK_ID + "\"").andExpect(status().isNoContent());

        lookup(member.getId())
                .andExpect(jsonPath("$.results[0].status").value("SAVED"))
                .andExpect(jsonPath("$.results[0].slackId").value(SLACK_ID));

        verify(slackClient, never()).findSlackIdByEmail(anyString());
    }

    @Test
    void 빈_목록이면_400이다() throws Exception {
        mockMvc.perform(post("/v1/admin/members/slack-ids/lookup")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- 부원 추가 시 자동 조회 ----------

    @Test
    void 부원을_추가하면_이메일로_Slack_ID를_찾아_저장한다() throws Exception {
        when(slackClient.findSlackIdByEmail("new-member@bcsd.club")).thenReturn(Optional.of(SLACK_ID));

        createMember("20249001", "new-member@bcsd.club").andExpect(status().isCreated());

        assertThat(memberRepository.findByStudentNumber("20249001").orElseThrow().getSlackId()).isEqualTo(SLACK_ID);
    }

    @Test
    void Slack_조회가_실패해도_부원_추가는_성공한다() throws Exception {
        when(slackClient.findSlackIdByEmail(anyString())).thenThrow(new RuntimeException("slack down"));

        createMember("20249002", "slack-down@bcsd.club").andExpect(status().isCreated());

        Member created = memberRepository.findByStudentNumber("20249002").orElseThrow();
        assertThat(created.getSlackId()).isNull();
    }

    @Test
    void 찾은_값이_중복이면_저장하지_않고_부원_추가는_성공한다() throws Exception {
        Member owner = saveMember("20240041", "기존주인", MemberRole.MEMBER);
        patchSlackId(owner.getId(), "\"" + OTHER_SLACK_ID + "\"").andExpect(status().isNoContent());
        when(slackClient.findSlackIdByEmail("dup@bcsd.club")).thenReturn(Optional.of(OTHER_SLACK_ID));

        createMember("20249003", "dup@bcsd.club").andExpect(status().isCreated());

        assertThat(memberRepository.findByStudentNumber("20249003").orElseThrow().getSlackId()).isNull();
    }

    // ---------- helpers ----------

    private org.springframework.test.web.servlet.ResultActions patchSlackId(Long memberId, String jsonValue)
            throws Exception {
        return mockMvc.perform(patch("/v1/admin/members/{id}/slack-id", memberId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"slackId\":" + jsonValue + "}"));
    }

    private org.springframework.test.web.servlet.ResultActions lookup(Long... memberIds) throws Exception {
        return mockMvc.perform(post("/v1/admin/members/slack-ids/lookup")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("memberIds", memberIds))));
    }

    private org.springframework.test.web.servlet.ResultActions createMember(String studentNumber, String email)
            throws Exception {
        String body = """
                {"name":"신입생","studentNumber":"%s","track":"BACKEND","memberType":"BEGINNER",
                 "generation":"25-상","university":"한국기술교육대학교","department":"컴퓨터공학부",
                 "email":"%s"}
                """.formatted(studentNumber, email);
        return mockMvc.perform(post("/v1/admin/members")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private Member saveMember(String studentNumber, String name, MemberRole role) {
        return memberRepository.save(Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name(name)
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email(studentNumber + "@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .role(role)
                .build());
    }

    private String login(String studentNumber) throws Exception {
        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentNumber":"%s","password":"%s","rememberMe":false}
                                """.formatted(studentNumber, RAW_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }
}
