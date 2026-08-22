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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.mail.transport=log")
class AdminMemberWelcomeMailIntegrationTest {

    private static final String RAW_PASSWORD = "AdminPass1";

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

    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        memberRepository.deleteAll();
        Member admin = Member.builder()
                .studentNumber("20230001")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("관리자")
                .track(Track.BACKEND)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("admin@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.ADMIN)
                .passwordChangedAt(Instant.now())
                .build();
        memberRepository.save(admin);

        String loginBody = """
                {"studentNumber":"20230001","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        adminAccessToken = objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    @Test
    void 관리자가_회원을_생성하면_환영_메일_발송_시각이_기록된다() throws Exception {
        String createBody = """
                {"name":"신입생","studentNumber":"20239999","track":"BACKEND","memberType":"REGULAR",
                 "generation":"24-하","university":"OO대학교","department":"컴퓨터공학과",
                 "email":"new-member@bcsd.club"}
                """;

        String responseBody = mockMvc.perform(post("/v1/admin/members")
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(responseBody);
        long memberId = json.get("id").asLong();

        Instant deadline = Instant.now().plusSeconds(5);
        Member reloaded;
        do {
            Thread.sleep(100);
            reloaded = memberRepository.findById(memberId).orElseThrow();
        } while (reloaded.getWelcomeMailSentAt() == null && Instant.now().isBefore(deadline));

        assertThat(reloaded.getWelcomeMailSentAt()).isNotNull();

        int reverted = memberRepository.markWelcomeMailSentIfCurrent(memberId, null, reloaded.getPasswordChangedAt());
        assertThat(reverted).isEqualTo(1);
        assertThat(memberRepository.findById(memberId).orElseThrow().getWelcomeMailSentAt()).isNull();
    }
}
