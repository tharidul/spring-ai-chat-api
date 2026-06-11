package com.tharidulakmal.springaichatapi.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SendMessageRequest(

        @NotBlank(message = "Message cannot be blank")
        @Size(max = 10000, message = "Message cannot exceed 10000 characters")
        String message,

        UUID agentId

) {}