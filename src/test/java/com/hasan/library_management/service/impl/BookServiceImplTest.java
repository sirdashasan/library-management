package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.response.BookResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.BookMapper;
import com.hasan.library_management.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        book = new Book();
        book.setId(bookId);
        book.setTitle("The Resonance Key");
        book.setAuthor("Marie D. Jones");
        book.setIsbn("9781601631234");
        book.setGenre("Metaphysical");
        book.setPublicationDate(LocalDate.of(2010, 5, 20));
        book.setAvailable(true);
    }

    // *** getAllBooks Tests ***
    @Test
    void getAllBooks_shouldReturnBookList() {
        // Arrange
        Book anotherBook = new Book();
        anotherBook.setId(UUID.randomUUID());
        anotherBook.setTitle("The Little Prince");
        anotherBook.setAuthor("Antoine de Saint-Exupéry");
        anotherBook.setIsbn("9780156012195");
        anotherBook.setGenre("Fable");
        anotherBook.setPublicationDate(LocalDate.of(1943, 4, 6));
        anotherBook.setAvailable(true);

        book.setAvailable(true);

        when(bookRepository.findAll()).thenReturn(List.of(book, anotherBook));

        when(bookMapper.toResponseDto(book)).thenReturn(
                new BookResponseDto(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getGenre(),
                        book.isAvailable()
                )
        );

        when(bookMapper.toResponseDto(anotherBook)).thenReturn(
                new BookResponseDto(
                        anotherBook.getId(),
                        anotherBook.getTitle(),
                        anotherBook.getAuthor(),
                        anotherBook.getIsbn(),
                        anotherBook.getPublicationDate(),
                        anotherBook.getGenre(),
                        anotherBook.isAvailable()
                )
        );

        // Act
        var result = bookService.getAllBooks();

        // Assert
        assertEquals(2, result.size());
        assertEquals("The Resonance Key", result.get(0).getTitle());
        assertEquals("The Little Prince", result.get(1).getTitle());
    }

    // *** getBookById Tests ***
    @Test
    void getBookById_shouldReturnBook_whenExists() {
        // Arrange
        when(bookRepository.findById(bookId)).thenReturn(java.util.Optional.of(book));
        when(bookMapper.toResponseDto(book)).thenReturn(
                new BookResponseDto(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getGenre(),
                        book.isAvailable()
                )
        );

        // Act
        var result = bookService.getBookById(bookId);

        // Assert
        assertNotNull(result);
        assertEquals("The Resonance Key", result.getTitle());
        assertEquals("Marie D. Jones", result.getAuthor());
        assertEquals(bookId, result.getId());
    }

    @Test
    void getBookById_shouldThrowException_whenNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(bookRepository.findById(unknownId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        var exception = assertThrows(ApiException.class, () -> bookService.getBookById(unknownId));
        assertEquals("Book not found with id: " + unknownId, exception.getMessage());
    }

    // *** createBook Tests ***
    @Test
    void createBook_shouldCreate_whenIsbnNotExists() {
        // Arrange
        var requestDto = new com.hasan.library_management.dto.request.BookRequestDto();
        requestDto.setTitle("The Resonance Key");
        requestDto.setAuthor("Marie D. Jones");
        requestDto.setIsbn("9781601631234");
        requestDto.setGenre("Metaphysical");
        requestDto.setPublicationDate(LocalDate.of(2010, 5, 20));

        when(bookRepository.findByIsbnContainingIgnoreCase("9781601631234", org.springframework.data.domain.Pageable.ofSize(1)))
                .thenReturn(org.springframework.data.domain.Page.empty());

        when(bookMapper.toEntity(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toResponseDto(book)).thenReturn(
                new BookResponseDto(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getPublicationDate(),
                        book.getGenre(),
                        book.isAvailable()
                )
        );

        // Act
        var result = bookService.createBook(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("The Resonance Key", result.getTitle());
        assertEquals("Marie D. Jones", result.getAuthor());
    }

    @Test
    void createBook_shouldThrowException_whenIsbnAlreadyExists() {
        // Arrange
        var requestDto = new com.hasan.library_management.dto.request.BookRequestDto();
        requestDto.setTitle("The Resonance Key");
        requestDto.setAuthor("Marie D. Jones");
        requestDto.setIsbn("9781601631234");
        requestDto.setGenre("Metaphysical");
        requestDto.setPublicationDate(LocalDate.of(2010, 5, 20));

        var existingPage = new org.springframework.data.domain.PageImpl<>(List.of(book));

        when(bookRepository.findByIsbnContainingIgnoreCase("9781601631234", org.springframework.data.domain.Pageable.ofSize(1)))
                .thenReturn(existingPage);

        // Act & Assert
        var exception = assertThrows(ApiException.class, () -> bookService.createBook(requestDto));
        assertEquals("A book with this ISBN already exists: 9781601631234", exception.getMessage());
    }

    // *** updateBook Tests ***
    @Test
    void updateBook_shouldUpdateBook_whenExists() {
        // Arrange
        UUID id = bookId;

        var requestDto = new BookRequestDto();
        requestDto.setTitle("Updated Title");
        requestDto.setAuthor("Updated Author");
        requestDto.setIsbn("1111111111111");
        requestDto.setGenre("Updated Genre");
        requestDto.setPublicationDate(LocalDate.of(2020, 1, 1));

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        when(bookMapper.toResponseDto(book)).thenReturn(
                new BookResponseDto(
                        id,
                        requestDto.getTitle(),
                        requestDto.getAuthor(),
                        requestDto.getIsbn(),
                        requestDto.getPublicationDate(),
                        requestDto.getGenre(),
                        true
                )
        );

        // Act
        var result = bookService.updateBook(id, requestDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        assertEquals("1111111111111", result.getIsbn());
    }

    @Test
    void updateBook_shouldThrowException_whenBookNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();

        var requestDto = new BookRequestDto();
        requestDto.setTitle("Some Title");
        requestDto.setAuthor("Some Author");
        requestDto.setIsbn("0000000000000");
        requestDto.setGenre("Some Genre");
        requestDto.setPublicationDate(LocalDate.of(2022, 1, 1));

        when(bookRepository.findById(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () ->
                bookService.updateBook(unknownId, requestDto)
        );

        assertEquals("Book not found with id: " + unknownId, exception.getMessage());
    }

    // *** deleteBook Tests ***
    @Test
    void deleteBook_shouldDeleteBook_whenExists() {
        // Arrange
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act & Assert
        assertDoesNotThrow(() -> bookService.deleteBook(bookId));
    }

    @Test
    void deleteBook_shouldThrowException_whenBookNotFound() {
        // Arrange
        UUID unknownId = UUID.randomUUID();
        when(bookRepository.findById(unknownId)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () ->
                bookService.deleteBook(unknownId)
        );

        assertEquals("Book not found with id: " + unknownId, exception.getMessage());
    }

    // *** searchByTitle Tests ***
    @Test
    void searchByTitle_shouldReturnPagedBooks() {
        // Arrange
        Pageable pageable = Pageable.ofSize(10);
        Book secondBook = new Book();
        secondBook.setId(UUID.randomUUID());
        secondBook.setTitle("The Little Prince");
        secondBook.setAuthor("Antoine de Saint-Exupéry");
        secondBook.setIsbn("9780156012195");
        secondBook.setGenre("Fiction");
        secondBook.setPublicationDate(LocalDate.of(1943, 4, 6));
        secondBook.setAvailable(true);

        List<Book> books = List.of(book, secondBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findByTitleContainingIgnoreCase("prince", pageable)).thenReturn(bookPage);

        when(bookMapper.toResponseDto(book)).thenReturn(new BookResponseDto(
                book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublicationDate(), book.getGenre(), book.isAvailable()
        ));

        when(bookMapper.toResponseDto(secondBook)).thenReturn(new BookResponseDto(
                secondBook.getId(), secondBook.getTitle(), secondBook.getAuthor(), secondBook.getIsbn(),
                secondBook.getPublicationDate(), secondBook.getGenre(), secondBook.isAvailable()
        ));

        // Act
        var result = bookService.searchByTitle("prince", pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("The Resonance Key", result.getContent().get(0).getTitle());
        assertEquals("The Little Prince", result.getContent().get(1).getTitle());
    }

    // *** searchByAuthor Tests ***
    @Test
    void searchByAuthor_shouldReturnPagedBooks() {
        // Arrange
        Pageable pageable = Pageable.ofSize(10);
        Book anotherBook = new Book();
        anotherBook.setId(UUID.randomUUID());
        anotherBook.setTitle("The Clean Coder");
        anotherBook.setAuthor("Robert C. Martin");
        anotherBook.setIsbn("9780137081073");
        anotherBook.setGenre("Programming");
        anotherBook.setPublicationDate(LocalDate.of(2011, 5, 13));
        anotherBook.setAvailable(true);

        List<Book> books = List.of(book, anotherBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findByAuthorContainingIgnoreCase("robert", pageable)).thenReturn(bookPage);

        when(bookMapper.toResponseDto(book)).thenReturn(new BookResponseDto(
                book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublicationDate(), book.getGenre(), book.isAvailable()
        ));

        when(bookMapper.toResponseDto(anotherBook)).thenReturn(new BookResponseDto(
                anotherBook.getId(), anotherBook.getTitle(), anotherBook.getAuthor(), anotherBook.getIsbn(),
                anotherBook.getPublicationDate(), anotherBook.getGenre(), anotherBook.isAvailable()
        ));

        // Act
        var result = bookService.searchByAuthor("robert", pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals("The Resonance Key", result.getContent().get(0).getTitle());
        assertEquals("The Clean Coder", result.getContent().get(1).getTitle());
    }

    // *** searchByIsbn Tests ***
    @Test
    void searchByIsbn_shouldReturnPagedBooks() {
        // Arrange
        Pageable pageable = Pageable.ofSize(10);

        Book anotherBook = new Book();
        anotherBook.setId(UUID.randomUUID());
        anotherBook.setTitle("The Resonance Law");
        anotherBook.setAuthor("John Doe");
        anotherBook.setIsbn("1234567890");
        anotherBook.setGenre("Personal Development");
        anotherBook.setPublicationDate(LocalDate.of(2020, 1, 1));
        anotherBook.setAvailable(true);

        List<Book> books = List.of(anotherBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findByIsbnContainingIgnoreCase("1234567890", pageable)).thenReturn(bookPage);

        when(bookMapper.toResponseDto(anotherBook)).thenReturn(new BookResponseDto(
                anotherBook.getId(), anotherBook.getTitle(), anotherBook.getAuthor(),
                anotherBook.getIsbn(), anotherBook.getPublicationDate(),
                anotherBook.getGenre(), anotherBook.isAvailable()
        ));

        // Act
        var result = bookService.searchByIsbn("1234567890", pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("The Resonance Law", result.getContent().get(0).getTitle());
        assertEquals("1234567890", result.getContent().get(0).getIsbn());
    }


    // *** searchByGenre Tests ***
    @Test
    void searchByGenre_shouldReturnPagedBooks() {
        // Arrange
        Pageable pageable = Pageable.ofSize(10);

        Book anotherBook = new Book();
        anotherBook.setId(UUID.randomUUID());
        anotherBook.setTitle("The Little Prince");
        anotherBook.setAuthor("Antoine de Saint-Exupéry");
        anotherBook.setIsbn("9780156012195");
        anotherBook.setGenre("Fiction");
        anotherBook.setPublicationDate(LocalDate.of(1943, 4, 6));
        anotherBook.setAvailable(true);

        List<Book> books = List.of(anotherBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findByGenreContainingIgnoreCase("fiction", pageable)).thenReturn(bookPage);

        when(bookMapper.toResponseDto(anotherBook)).thenReturn(new BookResponseDto(
                anotherBook.getId(), anotherBook.getTitle(), anotherBook.getAuthor(),
                anotherBook.getIsbn(), anotherBook.getPublicationDate(),
                anotherBook.getGenre(), anotherBook.isAvailable()
        ));

        // Act
        var result = bookService.searchByGenre("fiction", pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals("The Little Prince", result.getContent().get(0).getTitle());
        assertEquals("Fiction", result.getContent().get(0).getGenre());
    }
}