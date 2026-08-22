package com.bcsdlab.bcsdinternalapiv2.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberRole;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
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

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    void 관리자_승격은_재로그인_없이_즉시_반영되고_강등은_즉시_차단된다() throws Exception {
        Member member = Member.builder()
                .studentNumber("20234321")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("김민재")
                .track(Track.BACKEND)
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
}
