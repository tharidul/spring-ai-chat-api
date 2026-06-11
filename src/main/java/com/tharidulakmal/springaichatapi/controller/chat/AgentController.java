package com.tharidulakmal.springaichatapi.controller.chat;

import com.tharidulakmal.springaichatapi.dto.request.chat.CreateAgentRequest;
import com.tharidulakmal.springaichatapi.dto.response.chat.AgentResponse;
import com.tharidulakmal.springaichatapi.entity.chat.AiAgent;
import com.tharidulakmal.springaichatapi.exception.ResourceNotFoundException;
import com.tharidulakmal.springaichatapi.repository.chat.AiAgentRepository;
import com.tharidulakmal.springaichatapi.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AiAgentRepository aiAgentRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<AgentResponse>> createAgent(
            @Valid @RequestBody CreateAgentRequest request
    ) {
        AiAgent agent = new AiAgent();
        agent.setName(request.name());
        agent.setDescription(request.description());
        agent.setSystemPrompt(request.systemPrompt());
        agent.setTemperature(request.temperature());
        agent.setEnabled(request.enabled() != null ? request.enabled() : true);

        AiAgent saved = aiAgentRepository.save(agent);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agent created successfully", toResponse(saved)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgentResponse>>> getAllAgents() {
        List<AgentResponse> agents = aiAgentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Agents retrieved successfully", agents));
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<ApiResponse<AgentResponse>> getAgent(@PathVariable UUID agentId) {
        AiAgent agent = findAgent(agentId);
        return ResponseEntity.ok(ApiResponse.success("Agent retrieved successfully", toResponse(agent)));
    }

    @PutMapping("/{agentId}")
    public ResponseEntity<ApiResponse<AgentResponse>> updateAgent(
            @PathVariable UUID agentId,
            @Valid @RequestBody CreateAgentRequest request
    ) {
        AiAgent agent = findAgent(agentId);
        agent.setName(request.name());
        agent.setDescription(request.description());
        agent.setSystemPrompt(request.systemPrompt());
        agent.setTemperature(request.temperature());
        if (request.enabled() != null) {
            agent.setEnabled(request.enabled());
        }

        AiAgent updated = aiAgentRepository.save(agent);

        return ResponseEntity.ok(ApiResponse.success("Agent updated successfully", toResponse(updated)));
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAgent(@PathVariable UUID agentId) {
        AiAgent agent = findAgent(agentId);
        aiAgentRepository.delete(agent);
        return ResponseEntity.ok(ApiResponse.success("Agent deleted successfully", null));
    }

    private AiAgent findAgent(UUID agentId) {
        return aiAgentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    }

    private AgentResponse toResponse(AiAgent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getSystemPrompt(),
                agent.getTemperature(),
                agent.getEnabled(),
                agent.getCreatedAt(),
                agent.getUpdatedAt()
        );
    }
}
