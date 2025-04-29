package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.dto.response.UserResponseDto;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.mapper.UserMapper;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = UserMapper.toEntity(userRequestDto);
        user = userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateUser(UUID id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setName(userRequestDto.getName());
        existingUser.setEmail(userRequestDto.getEmail());
        existingUser.setPhoneNumber(userRequestDto.getPhoneNumber());
        existingUser.setRole(userRequestDto.getRole());

        userRepository.save(existingUser);
        return UserMapper.toResponseDto(existingUser);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }
}
