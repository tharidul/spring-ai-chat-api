package com.tharidulakmal.springaichatapi.entity.chat;

import com.tharidulakmal.springaichatapi.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_agents")
public class AiAgent extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Boolean enabled = true;
}