package com.tharidulakmal.springaichatapi.entity.chat;

import com.tharidulakmal.springaichatapi.entity.BaseEntity;
import com.tharidulakmal.springaichatapi.entity.auth.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_sessions")
public class ChatSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_agent_id", nullable = false)
    private AiAgent aiAgent;

    @Column(length = 255)
    private String title;
}