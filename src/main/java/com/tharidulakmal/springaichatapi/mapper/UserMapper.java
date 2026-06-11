package com.tharidulakmal.springaichatapi.mapper;

import com.tharidulakmal.springaichatapi.dto.request.auth.RegisterRequest;
import com.tharidulakmal.springaichatapi.dto.response.auth.RegisterResponse;
import com.tharidulakmal.springaichatapi.entity.auth.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toEntity(RegisterRequest request) {

        User user = new User();

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        return user;
    }

    public static RegisterResponse toResponse(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}