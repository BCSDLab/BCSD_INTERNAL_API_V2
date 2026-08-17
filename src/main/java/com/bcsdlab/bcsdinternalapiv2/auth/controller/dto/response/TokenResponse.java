package com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    public static TokenResponse of(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
