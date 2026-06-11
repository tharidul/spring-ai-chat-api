package com.tharidulakmal.springaichatapi.dto.response.chat;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(

        UUID id,
        String title,
        String agentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}