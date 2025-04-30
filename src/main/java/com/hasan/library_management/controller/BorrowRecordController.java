package com.hasan.library_management.controller;

import com.hasan.library_management.dto.request.BorrowRequestDto;
import com.hasan.library_management.dto.response.BorrowResponseDto;
import com.hasan.library_management.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/borrow-records")
@RequiredArgsConstructor
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    @GetMapping
    public ResponseEntity<List<BorrowResponseDto>> getAllBorrowRecords() {
        return ResponseEntity.ok(borrowRecordService.getAll());
    }

    @PostMapping
    public ResponseEntity<BorrowResponseDto> borrowBook(@RequestBody BorrowRequestDto requestDto) {
        return ResponseEntity.ok(borrowRecordService.borrowBook(requestDto));
    }

    @PutMapping("/return/{id}")
    public ResponseEntity<BorrowResponseDto> returnBook(@PathVariable UUID id) {
        return ResponseEntity.ok(borrowRecordService.returnBook(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowResponseDto>> getRecordsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(borrowRecordService.getBorrowRecordsByUserId(userId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowResponseDto>> getOverdueRecords() {
        return ResponseEntity.ok(borrowRecordService.getOverdueRecords());
    }
}
