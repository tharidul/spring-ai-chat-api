package com.tharidulakmal.springaichatapi.dto.response.chat;

import java.util.UUID;

public record SendMessageResponse(

        UUID sessionId,
        MessageResponse userMessage,
        MessageResponse aiMessage

) {}