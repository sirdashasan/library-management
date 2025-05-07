package com.hasan.library_management.controller;

import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.dto.response.AuthResponse;
import com.hasan.library_management.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with the provided details and returns a JWT token upon successful registration."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Email is already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Login a user",
            description = "Authenticates a user with email and password. Returns a JWT token upon successful authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
