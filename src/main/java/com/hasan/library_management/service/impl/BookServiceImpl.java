package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.response.BookResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.BookMapper;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponseDto getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ApiException("Book not found with id: " + id, HttpStatus.NOT_FOUND));
        return BookMapper.toResponseDto(book);
    }


    @Override
    public BookResponseDto createBook(BookRequestDto bookRequestDto) {
        boolean exists = bookRepository.findByIsbnContainingIgnoreCase(bookRequestDto.getIsbn(), Pageable.ofSize(1))
                .hasContent();

        if (exists) {
            throw new ApiException("A book with this ISBN already exists: " + bookRequestDto.getIsbn(), HttpStatus.CONFLICT);
        }

        Book book = BookMapper.toEntity(bookRequestDto);
        book = bookRepository.save(book);
        return BookMapper.toResponseDto(book);
    }


    @Override
    public BookResponseDto updateBook(UUID id, BookRequestDto bookRequestDto) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ApiException("Book not found with id: " + id, HttpStatus.NOT_FOUND));

        BookMapper.updateEntity(existingBook, bookRequestDto);
        existingBook = bookRepository.save(existingBook);
        return BookMapper.toResponseDto(existingBook);
    }

    @Override
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ApiException("Book not found with id: " + id, HttpStatus.NOT_FOUND));
        bookRepository.delete(book);
    }

    @Override
    public Page<BookResponseDto> searchByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(BookMapper::toResponseDto);
    }

    @Override
    public Page<BookResponseDto> searchByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable)
                .map(BookMapper::toResponseDto);
    }

    @Override
    public Page<BookResponseDto> searchByIsbn(String isbn, Pageable pageable) {
        return bookRepository.findByIsbnContainingIgnoreCase(isbn, pageable)
                .map(BookMapper::toResponseDto);
    }

    @Override
    public Page<BookResponseDto> searchByGenre(String genre, Pageable pageable) {
        return bookRepository.findByGenreContainingIgnoreCase(genre, pageable)
                .map(BookMapper::toResponseDto);
    }
}
