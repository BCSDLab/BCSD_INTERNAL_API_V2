package com.bcsdlab.bcsdinternalapiv2.auth.security;

import com.bcsdlab.bcsdinternalapiv2.global.config.RefreshTokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private static final String AUTH_PATH = "/v1/auth";

    private final RefreshTokenProperties refreshTokenProperties;

    public void addRefreshTokenCookie(HttpServletResponse response, String rawToken, long maxAgeSeconds) {
        Cookie cookie = buildCookie(rawToken, (int) maxAgeSeconds);
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = buildCookie("", 0);
        response.addCookie(cookie);
    }

    public Optional<String> readRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> refreshTokenProperties.cookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private Cookie buildCookie(String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(refreshTokenProperties.cookieName(), value);
        cookie.setHttpOnly(true);
        cookie.setSecure(refreshTokenProperties.cookieSecure());
        cookie.setPath(AUTH_PATH);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
