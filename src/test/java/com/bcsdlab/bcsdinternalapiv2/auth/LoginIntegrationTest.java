package com.bcsdlab.bcsdinternalapiv2.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import java.time.Instant;
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
class LoginIntegrationTest {

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
    void 잘못된_비밀번호는_401_AUTH_INVALID_CREDENTIALS_를_반환한다() throws Exception {
        String loginBody = """
                {"studentNumber":"20231234","password":"wrong-password","rememberMe":false}
                """;

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("학번 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 다섯번_연속_실패하면_올바른_비밀번호로도_423_AUTH_ACCOUNT_LOCKED_가_된다() throws Exception {
        String wrongLoginBody = """
                {"studentNumber":"20231234","password":"wrong-password","rememberMe":false}
                """;

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongLoginBody))
                    .andExpect(status().isUnauthorized());
        }

        String correctLoginBody = """
                {"studentNumber":"20231234","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctLoginBody))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.message").value("로그인 시도 초과로 계정이 잠겼습니다. 잠시 후 다시 시도해 주세요."));
    }

    @Test
    void 동시에_들어온_로그인_실패는_잠금_횟수를_잃지_않는다() throws Exception {
        String wrongLoginBody = """
                {"studentNumber":"20231234","password":"wrong-password","rememberMe":false}
                """;

        int concurrentAttempts = 10;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAttempts);
        List<Integer> statusCodes;
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < concurrentAttempts; i++) {
                futures.add(executor.submit(() -> mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongLoginBody))
                        .andReturn()
                        .getResponse()
                        .getStatus()));
            }
            statusCodes = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statusCodes.add(future.get());
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }

        assertThat(statusCodes).allMatch(status -> status == 401 || status == 423);

        Member reloaded = memberRepository.findByStudentNumber("20231234").orElseThrow();
        assertThat(reloaded.getLoginFailCount()).isGreaterThanOrEqualTo((short) 5);
        assertThat(reloaded.isLocked(Instant.now())).isTrue();

        String correctLoginBody = """
                {"studentNumber":"20231234","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctLoginBody))
                .andExpect(status().isLocked());
    }

    @Test
    void 동시에_들어온_성공과_실패_로그인은_서로를_막지_않는다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20236543")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("정하늘")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("mixed-login@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String wrongLoginBody = """
                {"studentNumber":"20236543","password":"wrong-password","rememberMe":false}
                """;
        String correctLoginBody = """
                {"studentNumber":"20236543","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        int concurrentAttempts = 6;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAttempts);
        List<Integer> statusCodes;
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < concurrentAttempts; i++) {
                String body = i == 0 ? correctLoginBody : wrongLoginBody;
                futures.add(executor.submit(() -> mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
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

        assertThat(statusCodes).allMatch(status -> status == 200 || status == 401 || status == 423);
    }

    @Test
    void 동시에_들어온_여러_성공_로그인은_교착되지_않는다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20235432")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("서지훈")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("concurrent-success-login@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String correctLoginBody = """
                {"studentNumber":"20235432","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        int concurrentAttempts = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAttempts);
        List<Integer> statusCodes;
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < concurrentAttempts; i++) {
                futures.add(executor.submit(() -> mockMvc.perform(post("/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(correctLoginBody))
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

        assertThat(statusCodes).allMatch(status -> status == 200);
    }

    @Test
    void LOCKED_상태인_계정은_비밀번호가_맞아도_로그인되지_않는다() throws Exception {
        Member lockedMember = Member.builder()
                .studentNumber("20230000")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("잠긴계정")
                .track(backend)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("locked@bcsd.club")
                .status(MemberStatus.LOCKED)
                .build();
        memberRepository.save(lockedMember);

        String loginBody = """
                {"studentNumber":"20230000","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isLocked());
    }
}
