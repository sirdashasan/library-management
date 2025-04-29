package com.hasan.library_management.controller;


import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.response.BookResponseDto;
import com.hasan.library_management.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }


    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@RequestBody BookRequestDto bookRequestDto) {
        return ResponseEntity.ok(bookService.createBook(bookRequestDto));
    }


    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(@PathVariable UUID id,
                                                      @RequestBody BookRequestDto bookRequestDto) {
        return ResponseEntity.ok(bookService.updateBook(id, bookRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // Search endpoints
    @GetMapping("/search/title")
    public ResponseEntity<Page<BookResponseDto>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.searchByTitle(title, pageable));
    }

    @GetMapping("/search/author")
    public ResponseEntity<Page<BookResponseDto>> searchByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.searchByAuthor(author, pageable));
    }

    @GetMapping("/search/isbn")
    public ResponseEntity<Page<BookResponseDto>> searchByIsbn(
            @RequestParam String isbn,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.searchByIsbn(isbn, pageable));
    }

    @GetMapping("/search/genre")
    public ResponseEntity<Page<BookResponseDto>> searchByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookService.searchByGenre(genre, pageable));
    }
}