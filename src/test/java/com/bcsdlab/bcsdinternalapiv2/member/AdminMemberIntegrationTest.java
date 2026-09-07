package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.AcademicStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminMemberIntegrationTest {

    private static final String RAW_PASSWORD = "Temp1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void jwtSecret(DynamicPropertyRegistry registry) {
        registry.add("app.jwt.secret", () -> "test-only-secret-key-not-for-production-32bytes-min");
    }

    @Autowired
    private MockMvc mockMvc;

    private TrackMaster backend;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 관리자_승격은_재로그인_없이_즉시_반영되고_강등은_즉시_차단된다() throws Exception {
        Member member = Member.builder()
                .studentNumber("20234321")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("김민재")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("promotion@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(member);

        String loginBody = """
                {"studentNumber":"20234321","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        String createBody = """
                {"name":"신입생","studentNumber":"20239998","track":"BACKEND","memberType":"REGULAR",
                 "generation":"24-하","university":"OO대학교","department":"컴퓨터공학과",
                 "email":"promotion-target@bcsd.club"}
                """;

        mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());

        memberRepository.updateRole(member.getId(), MemberRole.ADMIN);

        mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        memberRepository.updateRole(member.getId(), MemberRole.MEMBER);
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getRole())
                .isEqualTo(MemberRole.MEMBER);

        String secondCreateBody = """
                {"name":"신입생2","studentNumber":"20239997","track":"BACKEND","memberType":"REGULAR",
                 "generation":"24-하","university":"OO대학교","department":"컴퓨터공학과",
                 "email":"promotion-target-2@bcsd.club"}
                """;

        mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondCreateBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void 회원_생성_시_학부와_학적_상태와_활동여부가_저장된다() throws Exception {
        String adminToken = createAdminAndLogin("20230100");

        String createBody = """
                {"name":"신입생3","studentNumber":"20239996","track":"BACKEND","memberType":"REGULAR",
                 "generation":"24-하","university":"한국기술교육대학교","department":"컴퓨터공학부",
                 "academicStatus":"GRADUATED","active":false,"email":"grad@bcsd.club"}
                """;

        mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Member created = memberRepository.findByStudentNumber("20239996").orElseThrow();
        assertThat(created.getDepartment()).isEqualTo("컴퓨터공학부");
        assertThat(created.getAcademicStatus()).isEqualTo(AcademicStatus.GRADUATED);
        assertThat(created.isClubActive()).isFalse();
    }

    @Test
    void 학적_상태와_활동여부를_생략하면_기본값으로_생성된다() throws Exception {
        String adminToken = createAdminAndLogin("20230101");

        String createBody = """
                {"name":"신입생4","studentNumber":"20239995","track":"BACKEND","memberType":"REGULAR",
                 "generation":"24-하","university":"한국기술교육대학교","department":"컴퓨터공학부",
                 "email":"default@bcsd.club"}
                """;

        mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Member created = memberRepository.findByStudentNumber("20239995").orElseThrow();
        assertThat(created.getAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
        assertThat(created.isClubActive()).isTrue();
    }

    @Test
    void 학부를_입력하지_않으면_생성이_거부된다() throws Exception {
        String adminToken = createAdminAndLogin("20230102");

        String createBody = """
                {"name":"신입생5","studentNumber":"20239994","track":"BACKEND","memberType":"REGULAR",
                 "generation":"24-하","university":"한국기술교육대학교","email":"nodept@bcsd.club"}
                """;

        mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isBadRequest());
    }

    private String createAdminAndLogin(String studentNumber) throws Exception {
        Member admin = Member.builder()
                .studentNumber(studentNumber)
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("관리자")
                .track(backend)
                .generation("16")
                .memberType(MemberType.MENTOR)
                .university("한국기술교육대학교")
                .department("컴퓨터공학부")
                .email(studentNumber + "@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .build();
        memberRepository.save(admin);

        String loginBody = """
                {"studentNumber":"%s","password":"%s","rememberMe":false}
                """.formatted(studentNumber, RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }
}
