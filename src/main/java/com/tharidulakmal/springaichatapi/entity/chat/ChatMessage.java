package com.tharidulakmal.springaichatapi.entity.chat;

import com.tharidulakmal.springaichatapi.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    @Column(nullable = false)
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long inputTokens = 0L;

    @Column(nullable = false)
    private Long outputTokens = 0L;

    @Column(nullable = false)
    private Long totalTokens = 0L;
}