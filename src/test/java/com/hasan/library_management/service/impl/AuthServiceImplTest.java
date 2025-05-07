package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("123456")
                .phoneNumber("5551234567")
                .role(Role.PATRON)
                .build();

        authRequest = new AuthRequest("test@example.com", "123456");
    }

    // Register
    @Test
    void register_shouldReturnToken_whenEmailIsNotRegistered() {
        // Arrange
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken(registerRequest.getEmail(), registerRequest.getRole().name()))
                .thenReturn("mocked-jwt-token");

        // Act
        var response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(new User())); // user already exists

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> authService.register(registerRequest));
        assertEquals("Email is already registered", exception.getMessage());
    }


    // Login tests
    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Arrange
        var email = authRequest.getEmail();
        var password = authRequest.getPassword();

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email(email)
                .password("encodedPassword")
                .role(Role.PATRON)
                .phoneNumber("5551234567")
                .build();

        // mock authentication
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(email, "PATRON")).thenReturn("mocked-jwt-token");

        // Act
        var response = authService.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
    }


    @Test
    void login_shouldThrowException_whenAuthenticationFails() {
        // Arrange
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(authRequest));
    }

    @Test
    void login_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> authService.login(authRequest));
        assertEquals("User not found", exception.getMessage());
    }

}