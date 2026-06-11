package com.tharidulakmal.springaichatapi.security;

import com.tharidulakmal.springaichatapi.entity.auth.User;
import com.tharidulakmal.springaichatapi.exception.ResourceNotFoundException;
import com.tharidulakmal.springaichatapi.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return new CustomUserPrincipal(user);
    }
}