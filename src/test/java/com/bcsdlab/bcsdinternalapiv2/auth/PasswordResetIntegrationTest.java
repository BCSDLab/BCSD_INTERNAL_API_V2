package com.bcsdlab.bcsdinternalapiv2.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.model.PasswordResetToken;
import com.bcsdlab.bcsdinternalapiv2.auth.model.RefreshToken;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.PasswordResetTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.service.AuthService;
import com.bcsdlab.bcsdinternalapiv2.auth.service.PasswordResetService;
import com.bcsdlab.bcsdinternalapiv2.global.util.TokenHasher;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberType;
import com.bcsdlab.bcsdinternalapiv2.member.model.Track;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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
class PasswordResetIntegrationTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 토큰_재발급_직후_비밀번호가_재설정되면_새로_발급된_토큰도_폐기된다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20236666")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("최수진")
                .track(Track.BACKEND)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("reset-race@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String loginBody = """
                {"studentNumber":"20236666","password":"%s","rememberMe":false}
                """.formatted(RAW_PASSWORD);

        Cookie liveRefreshCookie = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("refreshToken");

        String rawResetToken = "raw-reset-token-for-race-test";
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .memberId(activeMember.getId())
                .tokenHash(TokenHasher.sha256Hex(rawResetToken))
                .expiresAt(Instant.now().plusSeconds(1800))
                .requestedIp("127.0.0.1")
                .createdAt(Instant.now())
                .build();
        passwordResetTokenRepository.save(resetToken);

        CountDownLatch readyLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> reissueFuture = executor.submit(() -> {
                readyLatch.countDown();
                readyLatch.await();
                MockHttpServletRequest mockRequest = new MockHttpServletRequest();
                mockRequest.setCookies(liveRefreshCookie);
                MockHttpServletResponse mockResponse = new MockHttpServletResponse();
                try {
                    authService.reissue(mockRequest, mockResponse);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
            Future<Exception> resetFuture = executor.submit(() -> {
                readyLatch.countDown();
                readyLatch.await();
                try {
                    passwordResetService.confirmReset(rawResetToken, "NewPass1234", "NewPass1234");
                    return null;
                } catch (Exception e) {
                    return e;
                }
            });

            reissueFuture.get(10, TimeUnit.SECONDS);
            Exception resetException = resetFuture.get(10, TimeUnit.SECONDS);
            assertThat(resetException).isNull();
        } finally {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }

        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(activeMember.getId());
        assertThat(tokens).isEmpty();
    }

    @Test
    void 한_토큰으로_비밀번호를_재설정하면_같은_회원의_다른_재설정_토큰도_모두_무효화된다() throws Exception {
        Member activeMember = Member.builder()
                .studentNumber("20235555")
                .password(passwordEncoder.encode(RAW_PASSWORD))
                .name("오세훈")
                .track(Track.BACKEND)
                .generation("24-하")
                .memberType(MemberType.REGULAR)
                .university("OO대학교")
                .email("dual-reset-token@bcsd.club")
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(activeMember);

        String rawTokenA = "raw-reset-token-a";
        String rawTokenB = "raw-reset-token-b";
        PasswordResetToken tokenA = PasswordResetToken.builder()
                .memberId(activeMember.getId())
                .tokenHash(TokenHasher.sha256Hex(rawTokenA))
                .expiresAt(Instant.now().plusSeconds(1800))
                .requestedIp("127.0.0.1")
                .createdAt(Instant.now())
                .build();
        PasswordResetToken tokenB = PasswordResetToken.builder()
                .memberId(activeMember.getId())
                .tokenHash(TokenHasher.sha256Hex(rawTokenB))
                .expiresAt(Instant.now().plusSeconds(1800))
                .requestedIp("127.0.0.2")
                .createdAt(Instant.now())
                .build();
        passwordResetTokenRepository.save(tokenA);
        passwordResetTokenRepository.save(tokenB);

        passwordResetService.confirmReset(rawTokenA, "FirstPass1234", "FirstPass1234");

        assertThatThrownBy(() -> passwordResetService.confirmReset(rawTokenB, "SecondPass1234", "SecondPass1234"))
                .isInstanceOf(AuthException.class);

        Member reloaded = memberRepository.findByStudentNumber("20235555").orElseThrow();
        assertThat(passwordEncoder.matches("FirstPass1234", reloaded.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("SecondPass1234", reloaded.getPassword())).isFalse();
    }
}
