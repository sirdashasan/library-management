package com.hasan.library_management.service;

import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.response.BorrowRecordResponseDto;

import java.util.List;
import java.util.UUID;

public interface BorrowRecordService {

    List<BorrowRecordResponseDto> getAll();
    BorrowRecordResponseDto borrowBook(BorrowRecordRequestDto requestDto);
    BorrowRecordResponseDto returnBook(UUID borrowRecordId);
    List<BorrowRecordResponseDto> getBorrowRecordsByUserId(UUID userId);
    List<BorrowRecordResponseDto> getOverdueRecords();
}