package com.hasan.library_management.mapper;

import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.dto.response.UserResponseDto;
import com.hasan.library_management.entity.User;

public class UserMapper {

    public static User toEntity(UserRequestDto dto) {

        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .phoneNumber(dto.getPhoneNumber())
                .role(dto.getRole())
                .build();
    }

    public static UserResponseDto toResponseDto(User user) {

        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }
}
