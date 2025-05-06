package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.AdminUserUpdateRequestDto;
import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.dto.response.UserResponseDto;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.UserMapper;
import com.hasan.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Hasan");
        user.setEmail("hasan@gmail.com");
        user.setPhoneNumber("5546006599");
        user.setRole(Role.PATRON);
    }

    // *** getAllUsers Tests ***
    @Test
    void getAllUsers_shouldReturnUserList() {
        // Arrange
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Cansu");
        user2.setEmail("cansu@gmail.com");
        user2.setPhoneNumber("5556547788");
        user2.setRole(Role.LIBRARIAN);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        when(userMapper.toResponseDto(user)).thenReturn(
                new com.hasan.library_management.dto.response.UserResponseDto(
                        user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getRole()
                )
        );

        when(userMapper.toResponseDto(user2)).thenReturn(
                new com.hasan.library_management.dto.response.UserResponseDto(
                        user2.getId(), user2.getName(), user2.getEmail(), user2.getPhoneNumber(), user2.getRole()
                )
        );

        // Act
        var result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Hasan", result.get(0).getName());
        assertEquals("Cansu", result.get(1).getName());
    }

    // *** getUserById Tests ***
    @Test
    void getUserById_shouldReturnUser_whenExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(
                new com.hasan.library_management.dto.response.UserResponseDto(
                        user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getRole()
                )
        );

        // Act
        var result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals("Hasan", result.getName());
        assertEquals(userId, result.getId());
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUserById(unknownId));
        assertEquals("User not found with id: " + unknownId, exception.getMessage());
    }

    // *** getOwnUserDetails Tests ***
    @Test
    void getOwnUserDetails_shouldReturnUser_whenExists() {
        // Arrange
        String email = "hasan@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));

        when(userMapper.toResponseDto(user)).thenReturn(
                new com.hasan.library_management.dto.response.UserResponseDto(
                        user.getId(), user.getName(), user.getEmail(), user.getPhoneNumber(), user.getRole()
                )
        );

        // Act
        var result = userService.getOwnUserDetails(email);

        // Assert
        assertNotNull(result);
        assertEquals("Hasan", result.getName());
        assertEquals("hasan@gmail.com", result.getEmail());
    }

    @Test
    void getOwnUserDetails_shouldThrowException_whenUserNotFound() {
        // Arrange
        String unknownEmail = "unknownuser@gmail.com";
        when(userRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () ->
                userService.getOwnUserDetails(unknownEmail)
        );

        assertEquals("User not found", exception.getMessage());
    }


    // *** updateUser Tests ***
    @Test
    void updateUser_shouldUpdateUser_whenExists() {
        // Arrange
        UUID id = userId;

        var requestDto = new AdminUserUpdateRequestDto();
        requestDto.setName("Updated Hasan");
        requestDto.setEmail("updatedhasan@gmail.com");
        requestDto.setPhoneNumber("5559990000");
        requestDto.setRole(Role.LIBRARIAN);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        when(userMapper.toResponseDto(user)).thenReturn(
                new UserResponseDto(
                        id,
                        requestDto.getName(),
                        requestDto.getEmail(),
                        requestDto.getPhoneNumber(),
                        requestDto.getRole()
                )
        );

        // Act
        var result = userService.updateUser(id, requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Hasan", result.getName());
        assertEquals("updatedhasan@gmail.com", result.getEmail());
        assertEquals(Role.LIBRARIAN, result.getRole());
    }


    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();

        var requestDto = new AdminUserUpdateRequestDto();
        requestDto.setName("Not Updated Hasan");
        requestDto.setEmail("updatedhasan@gmail.com");
        requestDto.setPhoneNumber("5559990000");
        requestDto.setRole(Role.LIBRARIAN);


        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () ->
                userService.updateUser(unknownId, requestDto)
        );

        assertEquals("User not found with id: " + unknownId, exception.getMessage());
    }


    // *** deleteUser Tests ***
    @Test
    void deleteUser_shouldDeleteUser_whenExists() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        assertDoesNotThrow(() -> userService.deleteUser(userId));
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () ->
                userService.deleteUser(unknownId)
        );

        assertEquals("User not found with id: " + unknownId, exception.getMessage());
    }
}