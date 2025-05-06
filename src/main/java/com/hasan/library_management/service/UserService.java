package com.hasan.library_management.service;

import com.hasan.library_management.dto.request.AdminUserUpdateRequestDto;
import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.dto.response.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(UUID id);
    UserResponseDto getOwnUserDetails(String email);

    UserResponseDto updateUser(UUID id, AdminUserUpdateRequestDto dto);
    void deleteUser(UUID id);
}
