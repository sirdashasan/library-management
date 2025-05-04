package com.hasan.library_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.repository.BorrowRecordRepository;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BorrowRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    private String token;
    private UUID userId;

    @BeforeAll
    void setup() throws Exception {
        String email = "borrow_admin@example.com";
        String password = "123456";

        // Create user if not already exists
        if (userRepository.findByEmail(email).isEmpty()) {
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setName("Borrow Admin");
            registerRequest.setEmail(email);
            registerRequest.setPassword(password);
            registerRequest.setPhoneNumber("5556667777");
            registerRequest.setRole(Role.LIBRARIAN);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());
        }

        // Perform login and retrieve token
        AuthRequest loginRequest = new AuthRequest(email, password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String json = loginResult.getResponse().getContentAsString();
        token = objectMapper.readTree(json).get("token").asText();
        userId = userRepository.findByEmail(email).orElseThrow().getId();
    }



    // *** getAllBorrowRecords Tests ***
    @Test
    void getAllBorrowRecords_shouldReturnOk_whenAuthorized() throws Exception {
        mockMvc.perform(get("/borrow-records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBorrowRecords_shouldReturnForbidden_whenNoToken() throws Exception {
        mockMvc.perform(get("/borrow-records"))
                .andExpect(status().isForbidden());
    }




    // *** borrowBook Tests ***
    @Test
    void borrowBook_shouldReturnOk_whenValidRequestAndAuthorized() throws Exception {
        // Create a book to borrow
        BookRequestDto book = new BookRequestDto();
        book.setTitle("Borrowable Book");
        book.setAuthor("Author B");
        book.setIsbn("1234567899998");
        book.setGenre("Genre");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));

        MvcResult bookResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(
                bookResult.getResponse().getContentAsString()).get("id").asText());

        BorrowRecordRequestDto borrowRequest = new BorrowRecordRequestDto();
        borrowRequest.setUserId(userId);
        borrowRequest.setBookId(bookId);
        borrowRequest.setBorrowDate(LocalDate.now());
        borrowRequest.setDueDate(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/borrow-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void borrowBook_shouldReturnForbidden_whenNoToken() throws Exception {
        BorrowRecordRequestDto request = new BorrowRecordRequestDto();
        request.setUserId(UUID.randomUUID());
        request.setBookId(UUID.randomUUID());
        request.setBorrowDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/borrow-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void borrowBook_shouldReturnBadRequest_whenDueDateIsNotFuture() throws Exception {
        BorrowRecordRequestDto request = new BorrowRecordRequestDto();
        request.setUserId(userId);
        request.setBookId(UUID.randomUUID());
        request.setBorrowDate(LocalDate.now());
        request.setDueDate(LocalDate.now()); // not future

        mockMvc.perform(post("/borrow-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }





    // *** returnBook Tests ***
    @Test
    void returnBook_shouldReturnOk_whenValidIdAndAuthorized() throws Exception {
        // 1. Create a book
        BookRequestDto book = new BookRequestDto();
        book.setTitle("Returnable Book");
        book.setAuthor("Author X");
        book.setIsbn("9991112223334");
        book.setGenre("ReturnTest");
        book.setPublicationDate(LocalDate.of(2022, 1, 1));

        MvcResult bookResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(bookResult.getResponse().getContentAsString()).get("id").asText());

        // 2. Borrow the book
        BorrowRecordRequestDto borrow = new BorrowRecordRequestDto();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(LocalDate.now().plusDays(10));

        MvcResult borrowResult = mockMvc.perform(post("/borrow-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isOk())
                .andReturn();

        UUID borrowId = UUID.fromString(objectMapper.readTree(borrowResult.getResponse().getContentAsString()).get("id").asText());

        // 3. Return the book
        mockMvc.perform(put("/borrow-records/return/" + borrowId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void returnBook_shouldReturnNotFound_whenRecordDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(put("/borrow-records/return/" + nonExistentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }


    @Test
    void returnBook_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        UUID dummyId = UUID.randomUUID();

        mockMvc.perform(put("/borrow-records/return/" + dummyId))
                .andExpect(status().isForbidden());
    }





    // *** getRecordsByUserId Tests ***
    @Test
    void getRecordsByUserId_shouldReturnList_whenValidUserAndAuthorized() throws Exception {
        // 1. Create a book
        BookRequestDto book = new BookRequestDto();
        book.setTitle("User Book");
        book.setAuthor("User Author");
        book.setIsbn("1212121212121");
        book.setGenre("UserGenre");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));

        MvcResult bookResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(bookResult.getResponse().getContentAsString()).get("id").asText());

        // 2. Borrow the book for the user
        BorrowRecordRequestDto borrow = new BorrowRecordRequestDto();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(LocalDate.now().plusDays(14));

        mockMvc.perform(post("/borrow-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isOk());

        // 3. Get records by userId
        mockMvc.perform(get("/borrow-records/user/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    void getRecordsByUserId_shouldReturnEmptyList_whenNoRecordsFound() throws Exception {
        // 1. Register a new user (with no borrow records)
        RegisterRequest register = new RegisterRequest();
        register.setName("Test Patron");
        register.setEmail("testpatron@example.com");
        register.setPassword("123456");
        register.setPhoneNumber("5550000000");
        register.setRole(Role.LIBRARIAN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // 2. Login and get JWT token
        AuthRequest login = new AuthRequest("testpatron@example.com", "123456");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        UUID userId = userRepository.findByEmail("testpatron@example.com").orElseThrow().getId();

        // 3. Call borrow records for this user (should return empty list)
        mockMvc.perform(get("/borrow-records/user/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

    }

    @Test
    void getRecordsByUserId_shouldReturnForbidden_whenNoToken() throws Exception {
        mockMvc.perform(get("/borrow-records/user/" + userId))
                .andExpect(status().isForbidden());
    }





    // *** getOwnRecords Tests ***
    @Test
    void getOwnRecords_shouldReturnList_whenUserHasBorrowedBooks() throws Exception {
        // Register new PATRON user
        String patronEmail = "ownrecords_patron@example.com";
        String patronPassword = "123456";

        // 1. Register Patron
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Patron User");
        registerRequest.setEmail(patronEmail);
        registerRequest.setPassword(patronPassword);
        registerRequest.setPhoneNumber("5551112222");
        registerRequest.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Login to get token
        AuthRequest authRequest = new AuthRequest(patronEmail, patronPassword);
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String patronToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        UUID patronUserId = userRepository.findByEmail(patronEmail).orElseThrow().getId();

        // 3. Create a book
        BookRequestDto book = new BookRequestDto();
        book.setTitle("Own Book");
        book.setAuthor("Own Author");
        book.setIsbn("9998887776661");
        book.setGenre("OwnGenre");
        book.setPublicationDate(LocalDate.of(2021, 5, 5));

        MvcResult bookResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token) // token: LIBRARIAN
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(bookResult.getResponse().getContentAsString()).get("id").asText());

        // 4. Borrow the book as PATRON
        BorrowRecordRequestDto borrow = new BorrowRecordRequestDto();
        borrow.setUserId(patronUserId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/borrow-records")
                        .header("Authorization", "Bearer " + patronToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isOk());

        // 5. Call /me endpoint
        mockMvc.perform(get("/borrow-records/me")
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(patronUserId.toString()));
    }


    @Test
    void getOwnRecords_shouldReturnEmptyList_whenNoRecordsExist() throws Exception {
        // Create a new user without borrow records
        RegisterRequest register = new RegisterRequest();
        register.setName("No Record User");
        register.setEmail("norecords@example.com");
        register.setPassword("123456");
        register.setPhoneNumber("0001112222");
        register.setRole(Role.PATRON);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // Login
        AuthRequest login = new AuthRequest("norecords@example.com", "123456");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String newToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Call /me with new user
        mockMvc.perform(get("/borrow-records/me")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getOwnRecords_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/borrow-records/me"))
                .andExpect(status().isForbidden());
    }





    // *** getOverdueRecords Tests ***
    @Test
    void getOverdueRecords_shouldReturnList_whenOverdueExists() throws Exception {
        // 1. Create a new book
        BookRequestDto book = new BookRequestDto();
        book.setTitle("Overdue Book");
        book.setAuthor("Overdue Author");
        book.setIsbn("9999999999001");
        book.setGenre("Horror");
        book.setPublicationDate(LocalDate.of(2015, 1, 1));

        MvcResult bookResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(
                bookResult.getResponse().getContentAsString()).get("id").asText());

        // 2. Borrow the book with a valid future due date (to pass validation)
        BorrowRecordRequestDto borrow = new BorrowRecordRequestDto();
        borrow.setUserId(userId);
        borrow.setBookId(bookId);
        borrow.setBorrowDate(LocalDate.now().minusDays(15));
        borrow.setDueDate(LocalDate.now().plusDays(5)); // valid dueDate for now

        MvcResult borrowResult = mockMvc.perform(post("/borrow-records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrow)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. Manually update dueDate in DB to simulate overdue record
        UUID recordId = UUID.fromString(objectMapper.readTree(
                borrowResult.getResponse().getContentAsString()).get("id").asText());

        BorrowRecord record = borrowRecordRepository.findById(recordId).orElseThrow();
        record.setDueDate(LocalDate.now().minusDays(5)); // make it overdue
        borrowRecordRepository.save(record);

        // 4. Fetch overdue borrow records
        mockMvc.perform(get("/borrow-records/overdue")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookTitle").value("Overdue Book"))
                .andExpect(jsonPath("$[0].returned").value(false));
    }


    @Test
    void getOverdueRecords_shouldReturnEmptyList_whenNoOverdue() throws Exception {

        borrowRecordRepository.deleteAll(); // Remove all borrow records to ensure there are no overdue records in the database before the test

        mockMvc.perform(get("/borrow-records/overdue")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getOverdueRecords_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/borrow-records/overdue"))
                .andExpect(status().isForbidden());
    }


}