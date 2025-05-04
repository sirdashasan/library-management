package com.hasan.library_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private final String email = "auth_test@example.com";
    private final String password = "123456";

    @BeforeEach
    void setup() throws Exception {
        // Ensure user is registered before each test
        if (userRepository.findByEmail(email).isEmpty()) {
            RegisterRequest request = new RegisterRequest();
            request.setName("Auth Tester");
            request.setEmail(email);
            request.setPassword(password);
            request.setPhoneNumber("5557778888");
            request.setRole(Role.LIBRARIAN);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    // *** register Tests ***
    @Test
    void register_shouldReturnOk_whenEmailIsNew() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("New User");
        request.setEmail("newuser@example.com");
        request.setPassword("password");
        request.setPhoneNumber("5558889999");
        request.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailAlreadyExists() throws Exception {
        RegisterRequest duplicateRequest = new RegisterRequest();
        duplicateRequest.setName("Duplicate User");
        duplicateRequest.setEmail(email); // same email
        duplicateRequest.setPassword(password);
        duplicateRequest.setPhoneNumber("5559991111");
        duplicateRequest.setRole(Role.LIBRARIAN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isBadRequest());
    }


    // *** login Tests ***
    @Test
    void login_shouldReturnOk_whenCredentialsAreCorrect() throws Exception {
        AuthRequest request = new AuthRequest(email, password);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsWrong() throws Exception {
        AuthRequest wrongPasswordRequest = new AuthRequest(email, "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnUnauthorized_whenEmailDoesNotExist() throws Exception {
        AuthRequest request = new AuthRequest("notfound@example.com", "wrongpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
