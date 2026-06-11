package com.tharidulakmal.springaichatapi.controller.chat;

import com.tharidulakmal.springaichatapi.dto.request.chat.SendMessageRequest;
import com.tharidulakmal.springaichatapi.dto.response.chat.MessageResponse;
import com.tharidulakmal.springaichatapi.dto.response.chat.SendMessageResponse;
import com.tharidulakmal.springaichatapi.dto.response.chat.SessionResponse;
import com.tharidulakmal.springaichatapi.security.CustomUserPrincipal;
import com.tharidulakmal.springaichatapi.service.chat.ChatService;
import com.tharidulakmal.springaichatapi.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<SendMessageResponse>> startSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody SendMessageRequest request
    ) {
        SendMessageResponse response = chatService.startSession(principal.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session started successfully", response));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<SendMessageResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        SendMessageResponse response = chatService.sendMessage(principal.getId(), sessionId, request);

        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", response));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getSessions(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<SessionResponse> sessions = chatService.getSessions(principal.getId());

        return ResponseEntity.ok(ApiResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        List<MessageResponse> messages = chatService.getMessages(principal.getId(), sessionId);

        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID sessionId
    ) {
        chatService.deleteSession(principal.getId(), sessionId);

        return ResponseEntity.ok(ApiResponse.success("Session deleted successfully", null));
    }
}
