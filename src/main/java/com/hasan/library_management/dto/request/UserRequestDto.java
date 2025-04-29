package com.hasan.library_management.dto.request;

import com.hasan.library_management.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {

    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private Role role;
}
