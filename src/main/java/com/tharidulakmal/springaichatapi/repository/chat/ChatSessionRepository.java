package com.tharidulakmal.springaichatapi.repository.chat;

import com.tharidulakmal.springaichatapi.entity.chat.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    List<ChatSession> findByUserId(UUID userId);

    // FIX: Return a List of ChatSession entities, not a ChatClientRequestSpec
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);
}