package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.dto.response.UserResponseDto;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.UserMapper;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND));
        return UserMapper.toResponseDto(user);
    }


    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {

        userRepository.findByEmail(userRequestDto.getEmail()).ifPresent(u -> {
            throw new ApiException("Email is already registered: " + userRequestDto.getEmail(), HttpStatus.CONFLICT);
        });

        User user = UserMapper.toEntity(userRequestDto);
        user = userRepository.save(user);
        return UserMapper.toResponseDto(user);
    }


    @Override
    public UserResponseDto updateUser(UUID id, UserRequestDto userRequestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND));

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
                .orElseThrow(() -> new ApiException("User not found with id: " + id, HttpStatus.NOT_FOUND));
        userRepository.delete(user);
    }
}
