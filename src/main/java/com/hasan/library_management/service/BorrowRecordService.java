package com.hasan.library_management.service;

import com.hasan.library_management.dto.request.BorrowRequestDto;
import com.hasan.library_management.dto.response.BorrowResponseDto;

import java.util.List;
import java.util.UUID;

public interface BorrowRecordService {

    List<BorrowResponseDto> getAll();
    BorrowResponseDto borrowBook(BorrowRequestDto requestDto);
    BorrowResponseDto returnBook(UUID borrowRecordId);
    List<BorrowResponseDto> getBorrowRecordsByUserId(UUID userId);
    List<BorrowResponseDto> getOverdueRecords();
}