package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.dto.response.AuthResponse;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.security.JwtUtil;
import com.hasan.library_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed. Email already registered: {}", request.getEmail());
            throw new ApiException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("User attempting to login with email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed. User not found: {}", request.getEmail());
                    return new ApiException("User not found", HttpStatus.UNAUTHORIZED);
                });

        log.info("Login successful for user: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token);
    }
}
