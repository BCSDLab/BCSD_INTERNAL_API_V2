package com.bcsdlab.bcsdinternalapiv2.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
class InitialSetupIntegrationTest {

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
    private TrackMasterRepository trackMasterRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        backend = trackMasterRepository.findByCode("BACKEND").orElseThrow();
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();
        Member member = Member.builder()
                .studentNumber("20231234")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("홍길동")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("test@bcsd.club")
                .build();
        memberRepository.save(member);
    }

    @Test
    void 최초_로그인_계정은_setup_scope_토큰을_받고_일반_API는_403이_된다() throws Exception {
        String loginBody = """
                {"studentNumber":"20231234","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_SETUP"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(responseBody);
        String accessToken = json.get("accessToken").asText();

        mockMvc.perform(get("/v1/members/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("최초 로그인 정보 입력이 필요합니다."));

        mockMvc.perform(get("/v1/members/me/initial-setup")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_SETUP"));
    }

    @Test
    void 동시에_들어온_초기_설정_완료_요청은_한_번만_성공한다() throws Exception {
        String loginBody = """
                {"studentNumber":"20231234","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        String responseBody = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String setupAccessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        int concurrentAttempts = 2;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAttempts);
        List<Integer> statusCodes;
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < concurrentAttempts; i++) {
                String setupBody = """
                        {"phoneNumber":"01012345678","email":"setup@bcsd.club","githubId":null,
                         "newPassword":"NewPass%d1","newPasswordConfirm":"NewPass%d1"}
                        """.formatted(i, i);
                futures.add(executor.submit(() -> mockMvc.perform(post("/v1/members/me/initial-setup")
                                .header("Authorization", "Bearer " + setupAccessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(setupBody))
                        .andReturn()
                        .getResponse()
                        .getStatus()));
            }
            statusCodes = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statusCodes.add(future.get(10, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertThat(statusCodes).filteredOn(status -> status == 200).hasSize(1);
        assertThat(statusCodes).filteredOn(status -> status == 401 || status == 409).hasSize(1);

        Member reloaded = memberRepository.findByStudentNumber("20231234").orElseThrow();
        assertThat(reloaded.isActive()).isTrue();

        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(reloaded.getId());
        assertThat(tokens).hasSize(1);
    }
}
