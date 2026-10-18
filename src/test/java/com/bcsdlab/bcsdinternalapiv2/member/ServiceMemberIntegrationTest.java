package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.IntegrationTestSupport;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Position;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.member.repository.PositionRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.member.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/** bbot 전용 명부 API(GET /v1/service/members)와 서비스 토큰 체인의 경계. */
@TestPropertySource(properties = {"app.mail.transport=log", "app.service.bbot-token=" + ServiceMemberIntegrationTest.SERVICE_TOKEN})
class ServiceMemberIntegrationTest extends IntegrationTestSupport {

    static final String SERVICE_TOKEN = "test-bbot-service-token-0123456789";
    private static final String RAW_PASSWORD = "Temp1234";
    private static final Set<String> EXPECTED_KEYS = Set.of(
            "id", "name", "slackId", "githubId", "track", "memberType", "clubActive", "academicStatus",
            "positionCodes");

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PositionRepository positionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TrackMaster backend;
    private TrackMaster frontend;

    @BeforeEach
    void setUp() {
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        frontend = trackMasterRepository.findByCode("FRONTEND").orElseThrow();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 서비스_토큰이면_탈퇴하지_않은_회원을_id_순으로_공개_필드만_내려준다() throws Exception {
        Member first = saveMember("20240001", "김하준", frontend, MemberStatus.ACTIVE, "U0FIRST0001", MemberRole.MEMBER,
                positionRepository.findAllByCodeIn(List.of("PRESIDENT", "FRONTEND_LEAD")));
        Member pending = saveMember("20240002", "이서연", backend, MemberStatus.PENDING_SETUP, null);
        saveMember("20240003", "탈퇴자", backend, MemberStatus.WITHDRAWN, "U0WITHDRAW1");

        String body = mockMvc.perform(get("/v1/service/members").header("Authorization", "Bearer " + SERVICE_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2))
                .andExpect(jsonPath("$.members[0].id").value(first.getId()))
                .andExpect(jsonPath("$.members[0].name").value("김하준"))
                .andExpect(jsonPath("$.members[0].slackId").value("U0FIRST0001"))
                .andExpect(jsonPath("$.members[0].githubId").value("github-0001"))
                .andExpect(jsonPath("$.members[0].track").value("FRONTEND"))
                .andExpect(jsonPath("$.members[0].memberType").value("REGULAR"))
                .andExpect(jsonPath("$.members[0].clubActive").value(true))
                .andExpect(jsonPath("$.members[0].academicStatus").value("ENROLLED"))
                .andExpect(jsonPath("$.members[0].positionCodes[0]").value("FRONTEND_LEAD"))
                .andExpect(jsonPath("$.members[0].positionCodes[1]").value("PRESIDENT"))
                .andExpect(jsonPath("$.members[0].positionCodes.length()").value(2))
                .andExpect(jsonPath("$.members[1].positionCodes.length()").value(0))
                .andExpect(jsonPath("$.members[1].id").value(pending.getId()))
                .andExpect(jsonPath("$.members[1].slackId").doesNotExist())
                .andExpect(jsonPath("$.members[1].track").value("BACKEND"))
                .andReturn().getResponse().getContentAsString();

        // 이메일·전화번호·학번·생년월일 등은 키 자체가 없어야 한다 — 허용 목록과 정확히 같은지 본다.
        JsonNode root = objectMapper.readTree(body);
        assertThat(fieldNames(root)).containsExactly("members");
        for (JsonNode member : root.get("members")) {
            assertThat(fieldNames(member)).isEqualTo(EXPECTED_KEYS);
        }
        assertThat(body).doesNotContain("@bcsd.club", "010-", "20240001", "20240002");
    }

    @Test
    void 토큰이_없으면_401이다() throws Exception {
        mockMvc.perform(get("/v1/service/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void 토큰이_틀리면_401이다() throws Exception {
        mockMvc.perform(get("/v1/service/members").header("Authorization", "Bearer " + SERVICE_TOKEN + "x"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/service/members").header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/service/members").header("Authorization", SERVICE_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 관리자_JWT로는_서비스_API를_쓸_수_없다() throws Exception {
        saveMember("20230001", "관리자", backend, MemberStatus.ACTIVE, null, MemberRole.ADMIN);
        String adminToken = login("20230001");

        mockMvc.perform(get("/v1/service/members").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 서비스_토큰으로는_관리자_API를_쓸_수_없다() throws Exception {
        mockMvc.perform(get("/v1/admin/members").header("Authorization", "Bearer " + SERVICE_TOKEN))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/members/directory").header("Authorization", "Bearer " + SERVICE_TOKEN))
                .andExpect(status().isUnauthorized());
    }

    private static Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private Member saveMember(String studentNumber, String name, TrackMaster track, MemberStatus status,
                              String slackId) {
        return saveMember(studentNumber, name, track, status, slackId, MemberRole.MEMBER);
    }

    private Member saveMember(String studentNumber, String name, TrackMaster track, MemberStatus status,
                              String slackId, MemberRole role) {
        return saveMember(studentNumber, name, track, status, slackId, role, List.of());
    }

    private Member saveMember(String studentNumber, String name, TrackMaster track, MemberStatus status,
                              String slackId, MemberRole role, List<Position> positions) {
        Member member = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name(name)
                .track(track)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .academicStatus(AcademicStatus.ENROLLED)
                .birthDate(LocalDate.of(2003, 3, 1))
                .email(studentNumber + "@bcsd.club")
                .phoneNumber("010-1234-" + studentNumber.substring(4))
                .githubId("github-" + studentNumber.substring(4))
                .status(status)
                .role(role)
                .positions(positions)
                .build();
        member.updateSlackId(slackId);
        return memberRepository.save(member);
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
