package com.bcsdlab.bcsdinternalapiv2.global.config;

import com.bcsdlab.bcsdinternalapiv2.auth.security.CustomJwtAuthenticationConverter;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtAccessDeniedHandler;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtAuthenticationEntryPoint;
import com.bcsdlab.bcsdinternalapiv2.auth.security.ServiceTokenAuthorizationManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;
    private final CorsProperties corsProperties;
    private final ServiceTokenProperties serviceTokenProperties;

    /** 운영은 12. 통합 테스트만 IntegrationTestSupport에서 낮춘다(해시에 비용이 기록돼 검증은 그대로 된다). */
    @Bean
    public PasswordEncoder passwordEncoder(@Value("${app.security.bcrypt-strength:12}") int strength) {
        return new BCryptPasswordEncoder(strength);
    }

    // 인터널 프론트가 access token은 Authorization 헤더로, refresh token은 httpOnly
    // 쿠키로 주고받는다(credentials: 'include') — Allow-Origin에 "*"를 쓸 수 없고
    // 명시적 origin + allowCredentials(true)가 필요하다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 내부 서비스(bbot) 전용 체인. {@code /v1/service/**}만 담당하고 기본 체인보다 먼저 매칭된다.
     * oauth2ResourceServer를 붙이지 않으므로 사용자 JWT로는 통과할 수 없고, 반대로 서비스 토큰은
     * 기본 체인에서 JWT 디코딩에 실패해 다른 경로에 쓸 수 없다. 브라우저가 부르지 않으므로 CORS도 열지 않는다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain serviceFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/v1/service/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().access(new ServiceTokenAuthorizationManager(serviceTokenProperties.bbotToken()))
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v1/auth/login", "/v1/auth/reissue", "/v1/auth/logout")
                        .permitAll()
                        .requestMatchers("/v1/auth/password/**").permitAll()
                        .requestMatchers("/v1/tracks/**").permitAll()
                        .requestMatchers("/v1/activity-categories/**").permitAll()
                        .requestMatchers("/v1/activities/**").permitAll()
                        .requestMatchers("/v1/games/**").permitAll()
                        .requestMatchers("/v1/home/**").permitAll()
                        .requestMatchers("/v1/members/me/initial-setup")
                        .hasAnyAuthority("SCOPE_PRE_ACTIVATION", "SCOPE_FULL")
                        .requestMatchers(HttpMethod.GET, "/v1/reservations/monthly-occupancy", "/v1/reservations/daily")
                        .permitAll()
                        .requestMatchers("/v1/admin/games/**").hasAuthority("SCOPE_FULL")
                        .requestMatchers("/v1/admin/track-pages/**").hasAuthority("SCOPE_FULL")
                        .requestMatchers("/v1/admin/curriculums/**").hasAuthority("SCOPE_FULL")
                        .requestMatchers("/v1/admin/weeks/**").hasAuthority("SCOPE_FULL")
                        .requestMatchers("/v1/admin/topics/**").hasAuthority("SCOPE_FULL")
                        .requestMatchers("/v1/admin/**").access(AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.hasAuthority("SCOPE_FULL"),
                                AuthorityAuthorizationManager.hasAuthority("ROLE_ADMIN")))
                        .anyRequest().hasAuthority("SCOPE_FULL")
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build();
    }
}
