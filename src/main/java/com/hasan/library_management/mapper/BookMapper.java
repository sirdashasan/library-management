package com.hasan.library_management.mapper;

import com.hasan.library_management.dto.request.BookRequestDto;
import com.hasan.library_management.dto.response.BookResponseDto;
import com.hasan.library_management.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(BookRequestDto dto) {
        return Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .isbn(dto.getIsbn())
                .publicationDate(dto.getPublicationDate())
                .genre(dto.getGenre())
                .available(true)
                .build();
    }

    public BookResponseDto toResponseDto(Book book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publicationDate(book.getPublicationDate())
                .genre(book.getGenre())
                .available(book.isAvailable())
                .build();
    }

    public void updateEntity(Book book, BookRequestDto dto) {
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublicationDate(dto.getPublicationDate());
        book.setGenre(dto.getGenre());
    }
}