package com.hasan.library_management.controller;

import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.response.BorrowRecordResponseDto;
import com.hasan.library_management.service.BorrowRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/borrow-records")
@RequiredArgsConstructor
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    @Operation(
            summary = "Get all borrow records",
            description = "Retrieves a list of all borrow records in the system. Accessible only by librarians."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrow records retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not have permission to access this resource"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    @GetMapping
    public ResponseEntity<List<BorrowRecordResponseDto>> getAllBorrowRecords() {
        return ResponseEntity.ok(borrowRecordService.getAll());
    }


    @Operation(
            summary = "Borrow a book",
            description = "Allows a user (patron or librarian) to borrow a book by providing the required details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book borrowed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not have permission to access this resource"),
            @ApiResponse(responseCode = "404", description = "Book or user not found")
    })
    @PostMapping
    public ResponseEntity<BorrowRecordResponseDto> borrowBook(@RequestBody @Valid BorrowRecordRequestDto requestDto) {
        return ResponseEntity.ok(borrowRecordService.borrowBook(requestDto));
    }


    @Operation(
            summary = "Return a borrowed book",
            description = "Allows a user (patron or librarian) to return a previously borrowed book using its borrow record ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid return request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not have permission to access this resource"),
            @ApiResponse(responseCode = "404", description = "Borrow record not found")
    })
    @PutMapping("/return/{id}")
    public ResponseEntity<BorrowRecordResponseDto> returnBook(@PathVariable UUID id) {
        return ResponseEntity.ok(borrowRecordService.returnBook(id));
    }


    @Operation(
            summary = "Get borrow records by user ID",
            description = "Retrieves all borrow records associated with a specific user ID. Only librarians can access this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrow records retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User or records not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not have permission to access this resource")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BorrowRecordResponseDto>> getRecordsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(borrowRecordService.getBorrowRecordsByUserId(userId));
    }

    @Operation(
            summary = "Get borrow records of the authenticated user",
            description = "Returns a list of borrow records for the currently authenticated user. Useful for patrons to view their own borrowing history."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrow records retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    @GetMapping("/me")
    public ResponseEntity<List<BorrowRecordResponseDto>> getOwnRecords(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(borrowRecordService.getOwnBorrowRecords(email));
    }

    @Operation(
            summary = "Get overdue borrow records",
            description = "Retrieves a list of borrow records for books that are overdue. Only librarians can access this endpoint."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue borrow records retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not have permission to access this resource"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token")
    })
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowRecordResponseDto>> getOverdueRecords() {
        return ResponseEntity.ok(borrowRecordService.getOverdueRecords());
    }

    @Operation(
            summary = "Generate overdue report",
            description = "Generates a formatted text report for all overdue books, logs it, and writes to a file. Accessible only by librarians."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - You do not have permission to access this resource")
    })
    @GetMapping("/overdue/report")
    public ResponseEntity<String> generateOverdueReport() {
        String report = borrowRecordService.generateOverdueReport();
        return ResponseEntity.ok(report);
    }
}
