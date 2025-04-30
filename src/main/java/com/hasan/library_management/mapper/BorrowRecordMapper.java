package com.hasan.library_management.mapper;

import com.hasan.library_management.dto.request.BorrowRequestDto;
import com.hasan.library_management.dto.response.BorrowResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.User;

public class BorrowRecordMapper {

    public static BorrowRecord toEntity(BorrowRequestDto dto, User user, Book book) {
        return BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(dto.getBorrowDate())
                .dueDate(dto.getDueDate())
                .returned(false)
                .build();
    }

    public static BorrowResponseDto toResponseDto(BorrowRecord record) {
        return BorrowResponseDto.builder()
                .id(record.getId())
                .userId(record.getUser().getId())
                .bookId(record.getBook().getId())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .returned(record.isReturned())
                .build();
    }
}