package com.tharidulakmal.springaichatapi.repository.chat;

import com.tharidulakmal.springaichatapi.entity.chat.AiAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiAgentRepository extends JpaRepository<AiAgent, UUID> {
    Optional<AiAgent> findFirstByEnabledTrue();
}