package com.tharidulakmal.springaichatapi.controller.auth;

import com.tharidulakmal.springaichatapi.dto.request.auth.LoginRequest;
import com.tharidulakmal.springaichatapi.dto.request.auth.RegisterRequest;
import com.tharidulakmal.springaichatapi.dto.response.auth.LoginResponse;
import com.tharidulakmal.springaichatapi.dto.response.auth.RegisterResponse;
import com.tharidulakmal.springaichatapi.service.auth.AuthService;
import com.tharidulakmal.springaichatapi.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "User registered successfully",
                                response
                        )
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(request)
                )
        );
    }
}