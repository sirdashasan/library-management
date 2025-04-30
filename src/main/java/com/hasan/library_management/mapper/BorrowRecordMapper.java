package com.hasan.library_management.mapper;

import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.response.BorrowRecordResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.User;

public class BorrowRecordMapper {

    public static BorrowRecord toEntity(BorrowRecordRequestDto dto, User user, Book book) {
        return BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(dto.getBorrowDate())
                .dueDate(dto.getDueDate())
                .returned(false)
                .build();
    }

    public static BorrowRecordResponseDto toResponseDto(BorrowRecord record) {
        return BorrowRecordResponseDto.builder()
                .userName(record.getUser().getName())
                .bookTitle(record.getBook().getTitle())
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