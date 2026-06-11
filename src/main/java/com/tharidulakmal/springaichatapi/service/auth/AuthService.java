package com.tharidulakmal.springaichatapi.service.auth;

import com.tharidulakmal.springaichatapi.dto.request.auth.LoginRequest;
import com.tharidulakmal.springaichatapi.dto.request.auth.RegisterRequest;
import com.tharidulakmal.springaichatapi.dto.response.auth.LoginResponse;
import com.tharidulakmal.springaichatapi.dto.response.auth.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);

}