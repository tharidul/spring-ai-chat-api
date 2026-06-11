package com.tharidulakmal.springaichatapi.dto.response.auth;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email,
        String firstName,
        String lastName
) { }