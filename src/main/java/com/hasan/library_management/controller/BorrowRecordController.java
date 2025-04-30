package com.hasan.library_management.controller;

import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.response.BorrowRecordResponseDto;
import com.hasan.library_management.service.BorrowRecordService;
import jakarta.validation.Valid;
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
    public ResponseEntity<List<BorrowRecordResponseDto>> getAllBorrowRecords() {
        return ResponseEntity.ok(borrowRecordService.getAll());
    }

    @PostMapping
    public ResponseEntity<BorrowRecordResponseDto> borrowBook(@RequestBody @Valid BorrowRecordRequestDto requestDto) {
        return ResponseEntity.ok(borrowRecordService.borrowBook(requestDto));
    }

    @PutMapping("/return/{id}")
    public ResponseEntity<BorrowRecordResponseDto> returnBook(@PathVariable UUID id) {
        return ResponseEntity.ok(borrowRecordService.returnBook(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowRecordResponseDto>> getRecordsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(borrowRecordService.getBorrowRecordsByUserId(userId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecordResponseDto>> getOverdueRecords() {
        return ResponseEntity.ok(borrowRecordService.getOverdueRecords());
    }
}
