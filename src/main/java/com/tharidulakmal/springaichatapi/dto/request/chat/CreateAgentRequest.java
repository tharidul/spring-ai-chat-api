package com.tharidulakmal.springaichatapi.dto.request.chat;

import jakarta.validation.constraints.*;

public record CreateAgentRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @NotBlank(message = "System prompt is required")
        String systemPrompt,

        @NotNull(message = "Temperature is required")
        @DecimalMin(value = "0.0", message = "Temperature must be at least 0.0")
        @DecimalMax(value = "2.0", message = "Temperature cannot exceed 2.0")
        Double temperature,

        Boolean enabled

) {}
