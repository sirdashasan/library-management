package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.response.BorrowRecordResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.BorrowRecordMapper;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.repository.BorrowRecordRepository;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.service.BookAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowRecordServiceImplTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookAvailabilityService bookAvailabilityService;

    @InjectMocks
    private BorrowRecordServiceImpl borrowRecordService;

    @Mock
    private BorrowRecordMapper borrowRecordMapper;

    private BorrowRecord borrowRecord;
    private User user;
    private Book book;
    private UUID recordId;



    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        recordId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Alice");

        book = new Book();
        book.setId(bookId);
        book.setTitle("The Alchemist");

        borrowRecord = new BorrowRecord();
        borrowRecord.setId(recordId);
        borrowRecord.setUser(user);
        borrowRecord.setBook(book);
        borrowRecord.setBorrowDate(LocalDate.of(2024, 4, 1));
        borrowRecord.setDueDate(LocalDate.of(2024, 4, 10));
        borrowRecord.setReturned(false);
    }

    // *** getAll Tests ***
    @Test
    void getAll_shouldReturnBorrowRecords() {
        // Arrange
        when(borrowRecordRepository.findAll()).thenReturn(List.of(borrowRecord));

        when(borrowRecordMapper.toResponseDto(borrowRecord)).thenReturn(new BorrowRecordResponseDto(
                borrowRecord.getBook().getTitle(),
                borrowRecord.getUser().getName(),
                borrowRecord.getId(),
                borrowRecord.getUser().getId(),
                borrowRecord.getBook().getId(),
                borrowRecord.getBorrowDate(),
                borrowRecord.getDueDate(),
                borrowRecord.getReturnDate(),
                borrowRecord.isReturned()
        ));

        // Act
        var result = borrowRecordService.getAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getUserName());
        assertEquals("The Alchemist", result.get(0).getBookTitle());
        assertFalse(result.get(0).isReturned());
    }

    // *** borrowBook Tests ***
    @Test
    void borrowBook_shouldCreateBorrowRecord_whenValidRequest() {
        // Arrange
        BorrowRecordRequestDto requestDto = new BorrowRecordRequestDto(
                user.getId(), book.getId(),
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 4, 10)
        );
        book.setAvailable(true);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(borrowRecordMapper.toEntity(requestDto, user, book)).thenReturn(borrowRecord);
        when(borrowRecordRepository.save(borrowRecord)).thenReturn(borrowRecord);
        when(borrowRecordMapper.toResponseDto(borrowRecord)).thenReturn(new BorrowRecordResponseDto(
                book.getTitle(), user.getName(), borrowRecord.getId(),
                user.getId(), book.getId(),
                borrowRecord.getBorrowDate(), borrowRecord.getDueDate(),
                null, false
        ));

        // Act
        var result = borrowRecordService.borrowBook(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(book.getTitle(), result.getBookTitle());
        assertEquals(user.getName(), result.getUserName());
    }

    @Test
    void borrowBook_shouldThrowException_whenBookNotFound() {
        // Arrange
        BorrowRecordRequestDto requestDto = new BorrowRecordRequestDto(
                user.getId(), UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(5)
        );

        when(bookRepository.findById(requestDto.getBookId())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.borrowBook(requestDto));
        assertEquals("Book not found with id: " + requestDto.getBookId(), ex.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenUserNotFound() {
        // Arrange
        BorrowRecordRequestDto requestDto = new BorrowRecordRequestDto(
                UUID.randomUUID(), book.getId(), LocalDate.now(), LocalDate.now().plusDays(5)
        );
        book.setAvailable(true);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findById(requestDto.getUserId())).thenReturn(Optional.empty());

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.borrowBook(requestDto));
        assertEquals("User not found with id: " + requestDto.getUserId(), ex.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenBookNotAvailable() {
        // Arrange
        book.setAvailable(false);
        BorrowRecordRequestDto requestDto = new BorrowRecordRequestDto(
                user.getId(), book.getId(), LocalDate.now(), LocalDate.now().plusDays(5)
        );

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.borrowBook(requestDto));
        assertEquals("Book is currently not available for borrowing", ex.getMessage());
    }


    @Test
    void borrowBook_shouldThrowException_whenUserHasAlreadyBorrowed5Books() {
        // Arrange
        // Simulate a borrow request where the user has already borrowed 5 books
        BorrowRecordRequestDto requestDto = new BorrowRecordRequestDto(
                user.getId(), book.getId(), LocalDate.now(), LocalDate.now().plusDays(7)
        );
        book.setAvailable(true);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Simulate that the user has already borrowed 5 books (not returned)
        when(borrowRecordRepository.countByUserIdAndReturnedFalse(user.getId())).thenReturn(5);

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.borrowBook(requestDto));

        // Assert that the exception message indicates book limit exceeded
        assertEquals("You have reached the maximum limit of 5 borrowed books.", ex.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenUserHasOverdueBooks() {
        // Arrange
        // Simulate a borrow request where the user has overdue books
        BorrowRecordRequestDto requestDto = new BorrowRecordRequestDto(
                user.getId(), book.getId(), LocalDate.now(), LocalDate.now().plusDays(7)
        );
        book.setAvailable(true);

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // Simulate that the user has borrowed 2 books
        when(borrowRecordRepository.countByUserIdAndReturnedFalse(user.getId())).thenReturn(2);

        // Simulate that one of the books is overdue
        when(borrowRecordRepository.existsByUserIdAndReturnedFalseAndDueDateBefore(user.getId(), LocalDate.now()))
                .thenReturn(true);

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.borrowBook(requestDto));

        // Assert that the exception message indicates overdue books
        assertEquals("You have overdue books. Please return them before borrowing more.", ex.getMessage());
    }



    // *** returnBook Tests ***
    @Test
    void returnBook_shouldMarkAsReturned_whenValidRequest() {
        // Arrange
        borrowRecord.setReturned(false);
        book.setAvailable(false);

        when(borrowRecordRepository.findById(borrowRecord.getId())).thenReturn(Optional.of(borrowRecord));
        when(borrowRecordRepository.save(borrowRecord)).thenReturn(borrowRecord);
        when(bookRepository.save(book)).thenReturn(book);
        when(borrowRecordMapper.toResponseDto(borrowRecord)).thenReturn(new BorrowRecordResponseDto(
                book.getTitle(), user.getName(), borrowRecord.getId(),
                user.getId(), book.getId(),
                borrowRecord.getBorrowDate(), borrowRecord.getDueDate(),
                LocalDate.now(), true
        ));

        // Act
        var result = borrowRecordService.returnBook(borrowRecord.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isReturned());
    }

    @Test
    void returnBook_shouldThrowException_whenRecordNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(borrowRecordRepository.findById(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.returnBook(unknownId));
        assertEquals("Borrow record not found with id: " + unknownId, ex.getMessage());
    }

    @Test
    void returnBook_shouldThrowException_whenAlreadyReturned() {
        // Arrange
        borrowRecord.setReturned(true);
        when(borrowRecordRepository.findById(borrowRecord.getId())).thenReturn(Optional.of(borrowRecord));

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () -> borrowRecordService.returnBook(borrowRecord.getId()));
        assertEquals("This book has already been returned", ex.getMessage());
    }

    // *** getBorrowRecordsByUserId Tests ***
    @Test
    void getBorrowRecordsByUserId_shouldReturnRecords_whenUserExists() {
        // Arrange
        UUID userId = user.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(borrowRecordRepository.findByUserId(userId)).thenReturn(List.of(borrowRecord));
        when(borrowRecordMapper.toResponseDto(borrowRecord)).thenReturn(
                new BorrowRecordResponseDto(
                        book.getTitle(), user.getName(), borrowRecord.getId(),
                        userId, book.getId(), borrowRecord.getBorrowDate(),
                        borrowRecord.getDueDate(), borrowRecord.getReturnDate(), borrowRecord.isReturned()
                )
        );

        // Act
        var result = borrowRecordService.getBorrowRecordsByUserId(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }

    @Test
    void getBorrowRecordsByUserId_shouldThrowException_whenUserNotFound() {
        // Arrange
        UUID unknownUserId = UUID.randomUUID();
        when(userRepository.existsById(unknownUserId)).thenReturn(false);

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () ->
                borrowRecordService.getBorrowRecordsByUserId(unknownUserId)
        );

        assertEquals("User not found with id: " + unknownUserId, ex.getMessage());
    }

    // *** getOwnBorrowRecords Tests ***
    @Test
    void getOwnBorrowRecords_shouldReturnRecords_whenUserExists() {
        // Arrange
        String email = "alice@example.com";
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(borrowRecordRepository.findByUserId(user.getId())).thenReturn(List.of(borrowRecord));
        when(borrowRecordMapper.toResponseDto(borrowRecord)).thenReturn(
                new BorrowRecordResponseDto(
                        book.getTitle(), user.getName(), borrowRecord.getId(),
                        user.getId(), book.getId(), borrowRecord.getBorrowDate(),
                        borrowRecord.getDueDate(), borrowRecord.getReturnDate(), borrowRecord.isReturned()
                )
        );

        // Act
        var result = borrowRecordService.getOwnBorrowRecords(email);

        // Assert
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getUserId());
        assertEquals("The Alchemist", result.get(0).getBookTitle());
    }

    @Test
    void getOwnBorrowRecords_shouldThrowException_whenUserNotFound() {
        // Arrange
        String unknownEmail = "unknown@example.com";
        when(userRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException ex = assertThrows(ApiException.class, () ->
                borrowRecordService.getOwnBorrowRecords(unknownEmail)
        );

        assertEquals("User not found with email: " + unknownEmail, ex.getMessage());
    }

    @Test
    void getOverdueRecords_shouldReturnOverdueRecords() {
        // Arrange
        borrowRecord.setDueDate(LocalDate.now().minusDays(5)); //past due date
        borrowRecord.setReturned(false);

        when(borrowRecordRepository.findByReturnedFalseAndDueDateBefore(LocalDate.now()))
                .thenReturn(List.of(borrowRecord));

        when(borrowRecordMapper.toResponseDto(borrowRecord)).thenReturn(
                new BorrowRecordResponseDto(
                        book.getTitle(), user.getName(), borrowRecord.getId(),
                        user.getId(), book.getId(), borrowRecord.getBorrowDate(),
                        borrowRecord.getDueDate(), borrowRecord.getReturnDate(), borrowRecord.isReturned()
                )
        );

        // Act
        var result = borrowRecordService.getOverdueRecords();

        // Assert
        assertEquals(1, result.size());
        assertEquals("The Alchemist", result.get(0).getBookTitle());
        assertFalse(result.get(0).isReturned());
    }

    // Overdue Report Tests
    @Test
    void generateOverdueReport_shouldReturnFormattedText_whenOverdueExists() {
        // Arrange
        borrowRecord.setDueDate(LocalDate.now().minusDays(5)); // Overdue
        borrowRecord.setReturned(false);

        when(borrowRecordRepository.findByReturnedFalseAndDueDateBefore(LocalDate.now()))
                .thenReturn(List.of(borrowRecord));

        // Act
        String report = borrowRecordService.generateOverdueReport();

        // Assert
        assertNotNull(report);
        assertTrue(report.contains("❗ Overdue Book Report"));
        assertTrue(report.contains(user.getName()));
        assertTrue(report.contains(book.getTitle()));
        assertTrue(report.contains("Returned: ❌ No"));
    }


}