package com.tharidulakmal.springaichatapi.service.chat;

import com.tharidulakmal.springaichatapi.dto.request.chat.SendMessageRequest;
import com.tharidulakmal.springaichatapi.dto.response.chat.MessageResponse;
import com.tharidulakmal.springaichatapi.dto.response.chat.SendMessageResponse;
import com.tharidulakmal.springaichatapi.dto.response.chat.SessionResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    SendMessageResponse startSession(UUID userId, SendMessageRequest request);

    SendMessageResponse sendMessage(UUID userId, UUID sessionId, SendMessageRequest request);

    List<SessionResponse> getSessions(UUID userId);

    List<MessageResponse> getMessages(UUID userId, UUID sessionId);

    void deleteSession(UUID userId, UUID sessionId);

}