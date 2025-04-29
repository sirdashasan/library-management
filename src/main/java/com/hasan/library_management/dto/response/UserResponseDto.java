package com.hasan.library_management.dto.response;

import com.hasan.library_management.entity.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
}