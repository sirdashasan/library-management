package com.hasan.library_management.dto.request;

import com.hasan.library_management.entity.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(
            regexp = "^(\\+\\d{1,3})?\\s?\\d{10}$",
            message = "Invalid phone number. Please enter a 10-digit number e.g.(5541234567)"
    )
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;


    @NotNull(message = "Role is required")
    private Role role;
}
