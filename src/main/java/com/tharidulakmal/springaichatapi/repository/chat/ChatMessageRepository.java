package com.tharidulakmal.springaichatapi.repository.chat;

import com.tharidulakmal.springaichatapi.entity.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(UUID sessionId);

    void deleteByChatSessionId(UUID id);
}