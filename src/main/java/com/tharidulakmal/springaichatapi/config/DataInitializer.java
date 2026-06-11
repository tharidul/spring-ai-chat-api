package com.tharidulakmal.springaichatapi.config;

import com.tharidulakmal.springaichatapi.entity.chat.AiAgent;
import com.tharidulakmal.springaichatapi.repository.chat.AiAgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the default AI agent on application startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AiAgentRepository aiAgentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Spring AI Chat Data Initializer ===");

        seedDefaultAgent();

        log.info("=== Data initialization complete ===");
    }

    private void seedDefaultAgent() {
        if (aiAgentRepository.findFirstByEnabledTrue().isEmpty()) {
            AiAgent agent = new AiAgent();
            agent.setName("Gemini Assistant");
            agent.setDescription("Default conversational AI assistant powered by Google Gemini.");
            agent.setSystemPrompt(
                    "You are a helpful, friendly, and knowledgeable AI assistant. " +
                    "Provide clear, concise, and accurate answers. " +
                    "If you don't know something, say so honestly."
            );
            agent.setTemperature(0.7);
            agent.setEnabled(true);

            aiAgentRepository.save(agent);
            log.info(">>> Default AI agent 'Gemini Assistant' created successfully!");
        } else {
            log.info("Active AI agent already exists. Skipping seed.");
        }
    }
}
