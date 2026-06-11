package com.tharidulakmal.springaichatapi.service.chat.impl;

import com.tharidulakmal.springaichatapi.dto.request.chat.SendMessageRequest;
import com.tharidulakmal.springaichatapi.dto.response.chat.MessageResponse;
import com.tharidulakmal.springaichatapi.dto.response.chat.SendMessageResponse;
import com.tharidulakmal.springaichatapi.dto.response.chat.SessionResponse;
import com.tharidulakmal.springaichatapi.entity.auth.User;
import com.tharidulakmal.springaichatapi.entity.chat.AiAgent;
import com.tharidulakmal.springaichatapi.entity.chat.ChatMessage;
import com.tharidulakmal.springaichatapi.entity.chat.ChatSession;
import com.tharidulakmal.springaichatapi.entity.chat.SenderType;
import com.tharidulakmal.springaichatapi.exception.ResourceNotFoundException;
import com.tharidulakmal.springaichatapi.exception.UnauthorizedException;
import com.tharidulakmal.springaichatapi.repository.auth.UserRepository;
import com.tharidulakmal.springaichatapi.repository.chat.AiAgentRepository;
import com.tharidulakmal.springaichatapi.repository.chat.ChatMessageRepository;
import com.tharidulakmal.springaichatapi.repository.chat.ChatSessionRepository;
import com.tharidulakmal.springaichatapi.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final AiAgentRepository aiAgentRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatClient.Builder chatClientBuilder;

    // ─── Start a brand-new session ───────────────────────────────────────────

    @Override
    @Transactional
    public SendMessageResponse startSession(UUID userId, SendMessageRequest request) {

        User user = getUser(userId);
        AiAgent agent = request.agentId() != null
                ? getAgentById(request.agentId())
                : getActiveAgent();

        // Create the new session
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setAiAgent(agent);
        // First message becomes the session title (truncated to 60 chars)
        session.setTitle(truncate(request.message(), 60));
        ChatSession savedSession = chatSessionRepository.save(session);

        return executeChat(savedSession, agent, user, request.message());
    }

    // ─── Continue an existing session ────────────────────────────────────────

    @Override
    @Transactional
    public SendMessageResponse sendMessage(UUID userId, UUID sessionId, SendMessageRequest request) {

        ChatSession session = getSessionForUser(userId, sessionId);
        AiAgent agent = session.getAiAgent();
        User user = session.getUser();

        return executeChat(session, agent, user, request.message());
    }

    // ─── Get all sessions for a user ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getSessions(UUID userId) {
        getUser(userId); // validates user exists
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    // ─── Get message history for a session ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID userId, UUID sessionId) {
        ChatSession session = getSessionForUser(userId, sessionId);
        return chatMessageRepository
                .findByChatSessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    // ─── Delete a session ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        ChatSession session = getSessionForUser(userId, sessionId);
        chatMessageRepository.deleteByChatSessionId(session.getId());
        chatSessionRepository.delete(session);
    }

    // ─── Core chat execution ─────────────────────────────────────────────────

    /**
     * Builds conversation history from the DB, sends it to Gemini via
     * Spring AI's MessageChatMemoryAdvisor, persists both messages, returns
     * the response.
     *
     * Why rebuild from DB instead of using an in-memory store?
     * Because in-memory state is lost on restart. Since we already persist
     * every message to PostgreSQL, we just replay them into Spring AI's
     * MessageWindowChatMemory on each call. This gives us durable memory
     * with zero extra infrastructure.
     */
    private SendMessageResponse executeChat(
            ChatSession session,
            AiAgent agent,
            User user,
            String userText
    ) {
        // 1. Load existing conversation history from DB into Spring AI's in-memory store
        ChatMemory chatMemory = buildMemoryFromDb(session.getId());

        // 1. Build the client (clone the builder to avoid mutating the shared singleton)
        ChatClient chatClient = chatClientBuilder
                .clone()
                .defaultSystem(agent.getSystemPrompt())
                .defaultOptions(GoogleGenAiChatOptions.builder()
                        .temperature(agent.getTemperature()))
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();

// 3. Then call
        String aiResponse = chatClient
                .prompt()
                .user(userText)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, session.getId().toString()))
                .call()
                .content();

        // 4. Persist user message
        ChatMessage savedUserMsg = chatMessageRepository.save(
                buildMessage(session, user.getId(), SenderType.USER, userText)
        );

        // 5. Persist AI reply
        ChatMessage savedAiMsg = chatMessageRepository.save(
                buildMessage(session, agent.getId(), SenderType.AI_AGENT, aiResponse)
        );

        return new SendMessageResponse(
                session.getId(),
                toMessageResponse(savedUserMsg),
                toMessageResponse(savedAiMsg)
        );
    }

    /**
     * Reads persisted messages from the DB and loads them into a fresh
     * MessageWindowChatMemory so Spring AI can inject them as conversation
     * history on the next request.
     */
    private ChatMemory buildMemoryFromDb(UUID sessionId) {
        List<ChatMessage> dbMessages =
                chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(sessionId);

        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(50) // keep last 50 messages in context
                .build();

        if (!dbMessages.isEmpty()) {
            List<Message> messages = new ArrayList<>();
            for (ChatMessage msg : dbMessages) {
                if (msg.getSenderType() == SenderType.USER) {
                    messages.add(new UserMessage(msg.getContent()));
                } else {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
            memory.add(sessionId.toString(), messages);
        }

        return memory;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private ChatMessage buildMessage(ChatSession session, UUID senderId,
                                     SenderType senderType, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setChatSession(session);
        msg.setSenderId(senderId);
        msg.setSenderType(senderType);
        msg.setContent(content);
        return msg;
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AiAgent getActiveAgent() {
        return aiAgentRepository.findFirstByEnabledTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active AI agent found"));
    }

    private AiAgent getAgentById(UUID agentId) {
        return aiAgentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    }

    private ChatSession getSessionForUser(UUID userId, UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this session");
        }
        return session;
    }

    private SessionResponse toSessionResponse(ChatSession session) {
        return new SessionResponse(
                session.getId(),
                session.getTitle(),
                session.getAiAgent().getName(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    private MessageResponse toMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getSenderType(),
                message.getCreatedAt()
        );
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}