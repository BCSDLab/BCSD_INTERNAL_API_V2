package com.bcsdlab.bcsdinternalapiv2.global.config;

import com.bcsdlab.bcsdinternalapiv2.auth.security.CustomJwtAuthenticationConverter;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtAccessDeniedHandler;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
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
                        .requestMatchers("/v1/members/me/initial-setup")
                        .hasAnyAuthority("SCOPE_PRE_ACTIVATION", "SCOPE_FULL")
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
