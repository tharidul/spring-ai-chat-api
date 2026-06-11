package com.tharidulakmal.springaichatapi.dto.response.chat;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgentResponse(

        UUID id,
        String name,
        String description,
        String systemPrompt,
        Double temperature,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}
