package com.hasan.library_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hasan.library_management.dto.request.AuthRequest;
import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.request.RegisterRequest;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "server.servlet.context-path="
})
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeAll
    void setup() throws Exception {
        // Register and login a LIBRARIAN user to get token
        String email = "bookadmin@example.com";
        String password = "123456";

        if (userRepository.findByEmail(email).isEmpty()) {
            RegisterRequest register = new RegisterRequest();
            register.setName("Book Admin");
            register.setEmail(email);
            register.setPassword(password);
            register.setPhoneNumber("5550001111");
            register.setRole(Role.LIBRARIAN);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(register)))
                    .andExpect(status().isOk());
        }

        AuthRequest login = new AuthRequest(email, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    // *** getAllBooks Tests ***
    @Test
    void getAllBooks_shouldReturnOk_whenAuthorized() throws Exception {
        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /* // This endpoint is publicly accessible, no need to expect forbidden error.
 @Test
void getAllBooks_shouldReturnForbidden_whenNoToken() throws Exception {
    mockMvc.perform(get("/books"))
           .andExpect(status().isForbidden());
 } */



    // *** getBookById Tests ***
    @Test
    void getBookById_shouldReturnOk_whenBookExistsAndAuthorized() throws Exception {
        // 1. Create a new book
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Test Book");
        bookRequest.setAuthor("Author A");
        bookRequest.setIsbn("1234567890123");
        bookRequest.setGenre("Test Genre");
        bookRequest.setPublicationDate(LocalDate.of(2020, 1, 1));

        MvcResult createResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Extract the book ID from the response
        String response = createResult.getResponse().getContentAsString();
        UUID createdBookId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // 3. Send GET request to fetch the book by ID
        mockMvc.perform(get("/books/" + createdBookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBookId.toString()))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void getBookById_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/books/" + nonExistentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /* // This endpoint is publicly accessible, no need to expect forbidden error.
    @Test
     void getBookById_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/books/" + fakeId))
                .andExpect(status().isForbidden());
       }*/




    // *** createBook Tests ***
    @Test
    void createBook_shouldReturnOk_whenValidRequestAndAuthorized() throws Exception {
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("New Book");
        bookRequest.setAuthor("Author X");
        bookRequest.setIsbn("1112223334445");
        bookRequest.setGenre("Fiction");
        bookRequest.setPublicationDate(LocalDate.of(2023, 5, 10));

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Book"));
    }

    @Test
    void createBook_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Tokenless Book");
        bookRequest.setAuthor("Author Y");
        bookRequest.setIsbn("1112223339999");
        bookRequest.setGenre("Drama");
        bookRequest.setPublicationDate(LocalDate.of(2023, 5, 10));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBook_shouldReturnBadRequest_whenMissingRequiredFields() throws Exception {
        BookRequestDto bookRequest = new BookRequestDto();

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isBadRequest());
    }




    // *** updateBook Tests ***
    @Test
    void updateBook_shouldUpdate_whenBookExistsAndValidRequest() throws Exception {
        // Create a book first
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Original Title");
        bookRequest.setAuthor("Author X");
        bookRequest.setIsbn("1111111111111");
        bookRequest.setGenre("Drama");
        bookRequest.setPublicationDate(LocalDate.of(2020, 1, 1));

        MvcResult createResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UUID createdBookId = UUID.fromString(objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asText());

        // Prepare update request
        BookRequestDto updateRequest = new BookRequestDto();
        updateRequest.setTitle("Updated Title");
        updateRequest.setAuthor("Author Y");
        updateRequest.setIsbn("2222222222222");
        updateRequest.setGenre("Science");
        updateRequest.setPublicationDate(LocalDate.of(2022, 5, 10));

        mockMvc.perform(put("/books/" + createdBookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.isbn").value("2222222222222"));
    }

    @Test
    void updateBook_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {
        UUID fakeId = UUID.randomUUID();

        BookRequestDto request = new BookRequestDto();
        request.setTitle("Ghost Book");
        request.setAuthor("Ghost Author");
        request.setIsbn("9999999999999");
        request.setGenre("Mystery");
        request.setPublicationDate(LocalDate.of(2023, 1, 1));

        mockMvc.perform(put("/books/" + fakeId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Create a book
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Valid Book");
        bookRequest.setAuthor("Valid Author");
        bookRequest.setIsbn("1234567899999");
        bookRequest.setGenre("History");
        bookRequest.setPublicationDate(LocalDate.of(2020, 1, 1));

        MvcResult createResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asText());

        // Invalid update (empty title)
        BookRequestDto invalidUpdate = new BookRequestDto();
        invalidUpdate.setTitle("");
        invalidUpdate.setAuthor("New Author");
        invalidUpdate.setIsbn("9999999999999");
        invalidUpdate.setGenre("Science");
        invalidUpdate.setPublicationDate(LocalDate.of(2024, 1, 1));

        mockMvc.perform(put("/books/" + bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }




    // *** deleteBook Tests ***
    @Test
    void deleteBook_shouldDelete_whenBookExistsAndAuthorized() throws Exception {
        // Create a book
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("To Be Deleted");
        bookRequest.setAuthor("Author D");
        bookRequest.setIsbn("1234567890011");
        bookRequest.setGenre("Thriller");
        bookRequest.setPublicationDate(LocalDate.of(2018, 8, 8));

        MvcResult createResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asText());

        // Delete the book
        mockMvc.perform(delete("/books/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Try to get the deleted book
        mockMvc.perform(get("/books/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(delete("/books/" + fakeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_shouldReturnForbidden_whenNoToken() throws Exception {
        // Create a book
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Unauthorized Delete");
        bookRequest.setAuthor("No Token Author");
        bookRequest.setIsbn("9876543210000");
        bookRequest.setGenre("Fantasy");
        bookRequest.setPublicationDate(LocalDate.of(2015, 6, 15));

        MvcResult createResult = mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("id").asText());

        // Try to delete without token
        mockMvc.perform(delete("/books/" + bookId))
                .andExpect(status().isForbidden());
    }




    // *** searchByTitle Tests ***
    @Test
    void searchByTitle_shouldReturnResults_whenMatchingBooksExist() throws Exception {
        // Create a book with a unique title
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Unique Search Title");
        bookRequest.setAuthor("Search Author");
        bookRequest.setIsbn("1111222233334");
        bookRequest.setGenre("SearchGenre");
        bookRequest.setPublicationDate(LocalDate.of(2021, 7, 7));

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk());

        // Search by title
        mockMvc.perform(get("/books/search/title")
                        .param("title", "Unique Search Title")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Unique Search Title"));
    }

    @Test
    void searchByTitle_shouldReturnEmpty_whenNoMatchesFound() throws Exception {
        mockMvc.perform(get("/books/search/title")
                        .param("title", "Nonexistent Title 123456")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

/* // This endpoint is publicly accessible, no need to expect forbidden error.
    @Test
    void searchByTitle_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/books/search/title")
                        .param("title", "Any Title"))
                .andExpect(status().isForbidden());
    } */




    // *** searchByAuthor Tests ***
    @Test
    void searchByAuthor_shouldReturnResults_whenMatchingBooksExist() throws Exception {
        // Create a book with a unique author
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Book with Author");
        bookRequest.setAuthor("Unique Author Name");
        bookRequest.setIsbn("2222333344445");
        bookRequest.setGenre("AuthorGenre");
        bookRequest.setPublicationDate(LocalDate.of(2021, 6, 6));

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk());

        // Search by author
        mockMvc.perform(get("/books/search/author")
                        .param("author", "Unique Author Name")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].author").value("Unique Author Name"));
    }

    @Test
    void searchByAuthor_shouldReturnEmpty_whenNoMatchesFound() throws Exception {
        mockMvc.perform(get("/books/search/author")
                        .param("author", "AuthorNotExistXYZ")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    /* // This endpoint is publicly accessible, no need to expect forbidden error.
 @Test
void searchByAuthor_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
   mockMvc.perform(get("/books/search/author")
                 .param("author", "Any Author"))
            .andExpect(status().isForbidden());
 } */



    // *** searchByIsbn Tests ***
    @Test
    void searchByIsbn_shouldReturnResults_whenMatchingBookExists() throws Exception {
        // Create a book with a known ISBN
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("ISBN Book");
        bookRequest.setAuthor("Author X");
        bookRequest.setIsbn("9876543210123");
        bookRequest.setGenre("ISBN Genre");
        bookRequest.setPublicationDate(LocalDate.of(2022, 3, 15));

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk());

        // Search by ISBN
        mockMvc.perform(get("/books/search/isbn")
                        .param("isbn", "9876543210123")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].isbn").value("9876543210123"));
    }

    @Test
    void searchByIsbn_shouldReturnEmpty_whenNoMatchFound() throws Exception {
        mockMvc.perform(get("/books/search/isbn")
                        .param("isbn", "0000000000000")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    /* // This endpoint is publicly accessible, no need to expect forbidden error.
    @Test
    void searchByIsbn_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/books/search/isbn")
                        .param("isbn", "9876543210123"))
                .andExpect(status().isForbidden());
    } */





    // *** searchByGenre Tests ***
    @Test
    void searchByGenre_shouldReturnResults_whenMatchingGenreExists() throws Exception {
        // Create a book with a known genre
        BookRequestDto bookRequest = new BookRequestDto();
        bookRequest.setTitle("Genre Book");
        bookRequest.setAuthor("Author G");
        bookRequest.setIsbn("9999999999999");
        bookRequest.setGenre("Fantasy");
        bookRequest.setPublicationDate(LocalDate.of(2021, 5, 10));

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk());

        // Search by genre
        mockMvc.perform(get("/books/search/genre")
                        .param("genre", "Fantasy")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].genre").value("Fantasy"));
    }

    @Test
    void searchByGenre_shouldReturnEmpty_whenNoMatchFound() throws Exception {
        mockMvc.perform(get("/books/search/genre")
                        .param("genre", "NonExistingGenre")
                        .param("page", "0")
                        .param("size", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // This endpoint is publicly accessible, no need to expect forbidden error.
   // @Test
   // void searchByGenre_shouldReturnForbidden_whenNoTokenProvided() throws Exception {
   //     mockMvc.perform(get("/books/search/genre")
    //                    .param("genre", "Fantasy"))
    //            .andExpect(status().isForbidden());
    // }


}