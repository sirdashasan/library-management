package com.hasan.library_management.dto.request;

import com.hasan.library_management.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
}