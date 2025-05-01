package com.hasan.library_management.dto.request;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}