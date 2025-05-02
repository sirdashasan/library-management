package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.response.BookResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.BookMapper;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public List<BookResponseDto> getAllBooks() {
        log.info("Fetching all books");
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponseDto getBookById(UUID id) {
        log.info("Fetching book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", id);
                    return new ApiException("Book not found with id: " + id, HttpStatus.NOT_FOUND);
                });
        return bookMapper.toResponseDto(book);
    }


    @Override
    public BookResponseDto createBook(BookRequestDto bookRequestDto) {
        log.info("Creating book with ISBN: {}", bookRequestDto.getIsbn());

        boolean exists = bookRepository.findByIsbnContainingIgnoreCase(bookRequestDto.getIsbn(), Pageable.ofSize(1))
                .hasContent();

        if (exists) {
            log.warn("Book with ISBN already exists: {}", bookRequestDto.getIsbn());
            throw new ApiException("A book with this ISBN already exists: " + bookRequestDto.getIsbn(), HttpStatus.CONFLICT);
        }

        Book book = bookMapper.toEntity(bookRequestDto);
        book = bookRepository.save(book);
        log.info("Book created successfully with ID: {}", book.getId());
        return bookMapper.toResponseDto(book);
    }


    @Override
    public BookResponseDto updateBook(UUID id, BookRequestDto bookRequestDto) {
        log.info("Updating book with ID: {}", id);

        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for update with ID: {}", id);
                    return new ApiException("Book not found with id: " + id, HttpStatus.NOT_FOUND);
                });

        bookMapper.updateEntity(existingBook, bookRequestDto);
        existingBook = bookRepository.save(existingBook);
        log.info("Book updated successfully with ID: {}", id);
        return bookMapper.toResponseDto(existingBook);
    }

    @Override
    public void deleteBook(UUID id) {
        log.info("Deleting book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found for deletion with ID: {}", id);
                    return new ApiException("Book not found with id: " + id, HttpStatus.NOT_FOUND);
                });
        bookRepository.delete(book);
        log.info("Book deleted with ID: {}", id);
    }

    @Override
    public Page<BookResponseDto> searchByTitle(String title, Pageable pageable) {
        log.info("Searching books by title: {}", title);
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(bookMapper::toResponseDto);
    }

    @Override
    public Page<BookResponseDto> searchByAuthor(String author, Pageable pageable) {
        log.info("Searching books by author: {}", author);
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(bookMapper::toResponseDto);
    }

    @Override
    public Page<BookResponseDto> searchByIsbn(String isbn, Pageable pageable) {
        log.info("Searching books by ISBN: {}", isbn);
        return bookRepository.findByIsbnContainingIgnoreCase(isbn, pageable)
                .map(bookMapper::toResponseDto);
    }

    @Override
    public Page<BookResponseDto> searchByGenre(String genre, Pageable pageable) {
        log.info("Searching books by genre: {}", genre);
        return bookRepository.findByGenreContainingIgnoreCase(genre, pageable)
                .map(bookMapper::toResponseDto);
    }
}
