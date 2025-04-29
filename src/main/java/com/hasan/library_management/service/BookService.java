package com.hasan.library_management.service;

import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.response.BookResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookService {
    List<BookResponseDto> getAllBooks();
    BookResponseDto getBookById(UUID id);
    BookResponseDto createBook(BookRequestDto bookRequestDto);
    BookResponseDto updateBook(UUID id, BookRequestDto bookRequestDto);
    void deleteBook(UUID id);

    Page<BookResponseDto> searchByTitle(String title, Pageable pageable);
    Page<BookResponseDto> searchByAuthor(String author, Pageable pageable);
    Page<BookResponseDto> searchByIsbn(String isbn, Pageable pageable);
    Page<BookResponseDto> searchByGenre(String genre, Pageable pageable);
}