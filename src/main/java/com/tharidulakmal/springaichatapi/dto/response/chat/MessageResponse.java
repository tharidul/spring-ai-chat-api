package com.tharidulakmal.springaichatapi.dto.response.chat;

import com.tharidulakmal.springaichatapi.entity.chat.SenderType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(

        UUID id,
        String content,
        SenderType senderType,
        LocalDateTime sentAt

) {}