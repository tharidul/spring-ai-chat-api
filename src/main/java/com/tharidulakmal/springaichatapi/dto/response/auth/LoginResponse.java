package com.tharidulakmal.springaichatapi.dto.response.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
