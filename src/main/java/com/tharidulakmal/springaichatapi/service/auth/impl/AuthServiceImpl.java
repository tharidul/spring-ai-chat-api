package com.tharidulakmal.springaichatapi.service.auth.impl;

import com.tharidulakmal.springaichatapi.dto.request.auth.LoginRequest;
import com.tharidulakmal.springaichatapi.dto.request.auth.RegisterRequest;
import com.tharidulakmal.springaichatapi.dto.response.auth.LoginResponse;
import com.tharidulakmal.springaichatapi.dto.response.auth.RegisterResponse;
import com.tharidulakmal.springaichatapi.entity.auth.Role;
import com.tharidulakmal.springaichatapi.entity.auth.User;
import com.tharidulakmal.springaichatapi.exception.BadRequestException;
import com.tharidulakmal.springaichatapi.exception.ResourceAlreadyExistsException;
import com.tharidulakmal.springaichatapi.exception.UnauthorizedException;
import com.tharidulakmal.springaichatapi.mapper.UserMapper;
import com.tharidulakmal.springaichatapi.repository.auth.UserRepository;
import com.tharidulakmal.springaichatapi.security.CustomUserPrincipal;
import com.tharidulakmal.springaichatapi.security.JwtService;
import com.tharidulakmal.springaichatapi.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }

        User user = UserMapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return UserMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(
            LoginRequest request
    ) {

        User user = userRepository.findByEmail(
                request.email()
        ).orElseThrow(
                () -> new UnauthorizedException(
                        "Invalid email or password"
                )
        );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {

            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        CustomUserPrincipal principal =
                new CustomUserPrincipal(user);

        String accessToken =
                jwtService.generateToken(principal);

        String refreshToken =
                jwtService.generateRefreshToken(principal);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900
        );
    }
}