package com.hasan.library_management.service;

import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}