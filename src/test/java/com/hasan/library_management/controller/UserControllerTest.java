package com.hasan.library_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.dto.request.UserRequestDto;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String token;
    private String email = "integration@example.com";
    private String password = "123456";

    @BeforeAll
    void setup() throws Exception {
        // Register user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Integration Tester");
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setPhoneNumber("5555555555");
        registerRequest.setRole(Role.LIBRARIAN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Login user
        AuthRequest loginRequest = new AuthRequest(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        token = objectMapper.readTree(json).get("token").asText();
    }


    // *** getAllUsers Tests ***
    @Test
    void getAll_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldReturnForbidden_whenNoToken() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_shouldReturnForbidden_whenUserIsPatron() throws Exception {
        // Register a new user with the role PATRON
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Patron User");
        registerRequest.setEmail("patron@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setPhoneNumber("5551231234");
        registerRequest.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Authenticate the PATRON user to get a token
        AuthRequest loginRequest = new AuthRequest("patron@example.com", "123456");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String patronToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Attempt to access the /users endpoint with a PATRON token
        // Only LIBRARIANs are authorized, so the response should be 403 Forbidden
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
    }



    // *** getUserById Tests ***
    @Test
    void getUserById_shouldReturnUser_whenAuthorizedAsLibrarian() throws Exception {
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();

        mockMvc.perform(get("/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("Integration Tester"));
    }

    @Test
    void getUserById_shouldReturnForbidden_whenUserIsPatron() throws Exception {
        // Register a new PATRON
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Patron Test");
        registerRequest.setEmail("patron2@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setPhoneNumber("5550000000");
        registerRequest.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Login as PATRON
        AuthRequest loginRequest = new AuthRequest("patron2@example.com", "123456");
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String patronToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
        UUID librarianUserId = userRepository.findByEmail(email).orElseThrow().getId();

        // Try accessing with patron token
        mockMvc.perform(get("/users/" + librarianUserId)
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/users/" + fakeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }



    // *** getOwnDetails Tests ***
    @Test
    void getOwnDetails_shouldReturnOk_whenAuthorized() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void getOwnDetails_shouldReturnForbidden_whenNoToken() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden()); //
    }

    @Test
    void getOwnDetails_shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }



    // *** updateUser Tests ***
    @Test
    void updateUser_shouldUpdate_whenLibrarianAndValidData() throws Exception {
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();

        UserRequestDto updateDto = new UserRequestDto();
        updateDto.setName("Updated Name");
        updateDto.setPhoneNumber("1112223333");
        updateDto.setEmail("integration@example.com");
        updateDto.setPassword("123456");
        updateDto.setRole(Role.LIBRARIAN);

        mockMvc.perform(put("/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.phoneNumber").value("1112223333"));
    }

    @Test
    void updateUser_shouldReturnForbidden_whenUserIsPatron() throws Exception {
        // Register a new user with the PATRON role
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Patron Update");
        registerRequest.setEmail("patronupdate@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setPhoneNumber("5551112222");
        registerRequest.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Login as the patron user and get a token
        AuthRequest loginRequest = new AuthRequest("patronupdate@example.com", "123456");
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String patronToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Get the ID of the librarian user created during setup
        UUID targetUserId = userRepository.findByEmail(email).orElseThrow().getId();

        // Attempt to update the librarian user with the patron token
        var updateBody = """
        {
            "name": "Hacked Name",
            "email": "hacked@example.com",
            "password": "newpass",
            "phoneNumber": "0001112222",
            "role": "LIBRARIAN"
        }
        """;

        // Expect forbidden since patron should not have permission
        mockMvc.perform(put("/users/" + targetUserId)
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Generate a random UUID that does not exist in the database
        UUID nonExistentId = UUID.randomUUID();

        // Build a valid update request body
        var updateBody = """
        {
            "name": "Ghost User",
            "email": "ghost@example.com",
            "password": "123456",
            "phoneNumber": "5559998888",
            "role": "LIBRARIAN"
        }
        """;

        // Attempt to update the non-existent user
        mockMvc.perform(put("/users/" + nonExistentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturnForbidden_whenNoToken() throws Exception {
        // Retrieve an existing user's ID to use in the request
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();

        // Build a valid update request body
        var updateBody = """
        {
            "name": "No Auth",
            "email": "noauth@example.com",
            "password": "123456",
            "phoneNumber": "0001112222",
            "role": "LIBRARIAN"
        }
        """;

        // Attempt to update without providing a token
        mockMvc.perform(put("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());
    }



    // *** deleteUser Tests ***
    @Test
    void deleteUser_shouldDelete_whenLibrarianAndValidId() throws Exception {
        // Register a new user to be deleted
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("User To Delete");
        registerRequest.setEmail("deleteuser@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setPhoneNumber("5554443322");
        registerRequest.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Find that user's ID
        UUID userIdToDelete = userRepository.findByEmail("deleteuser@example.com")
                .orElseThrow().getId();

        // Send DELETE request as librarian
        mockMvc.perform(delete("/users/" + userIdToDelete)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnForbidden_whenUserIsPatron() throws Exception {
        // Register a patron user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Patron Deleter");
        registerRequest.setEmail("patrondeleter@example.com");
        registerRequest.setPassword("123456");
        registerRequest.setPhoneNumber("5556667777");
        registerRequest.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // Login as patron
        AuthRequest loginRequest = new AuthRequest("patrondeleter@example.com", "123456");
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String patronToken = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Attempt to delete librarian user
        UUID targetUserId = userRepository.findByEmail(email).orElseThrow().getId();

        mockMvc.perform(delete("/users/" + targetUserId)
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Generate a random UUID not in the database
        UUID nonExistentId = UUID.randomUUID();

        // Attempt to delete with valid librarian token
        mockMvc.perform(delete("/users/" + nonExistentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturnForbidden_whenNoToken() throws Exception {
        // Get an existing user ID
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();

        // Attempt to delete without any token
        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isForbidden());
    }


}