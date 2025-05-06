package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.AdminUserUpdateRequestDto;
import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.dto.response.UserResponseDto;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.UserMapper;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND);
                });
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto getOwnUserDetails(String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return userMapper.toResponseDto(user);
    }




    @Override
    public UserResponseDto updateUser(UUID id, AdminUserUpdateRequestDto dto) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND);
                });

        userMapper.updateEntityFromAdminDto(existingUser, dto);

        userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", id);

        return userMapper.toResponseDto(existingUser);
    }


    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND);
                });

        userRepository.delete(user);
        log.info("User deleted with ID: {}", id);
    }
}
