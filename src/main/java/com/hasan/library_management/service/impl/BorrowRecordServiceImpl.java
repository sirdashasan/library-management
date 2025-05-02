package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.BorrowRecordRequestDto;
import com.hasan.library_management.dto.response.BorrowRecordResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.exceptions.ApiException;
import com.hasan.library_management.mapper.BorrowRecordMapper;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.repository.BorrowRecordRepository;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.service.BookAvailabilityService;
import com.hasan.library_management.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    // Used for emitting real-time book availability events (Reactive - WebFlux)
    private final BookAvailabilityService bookAvailabilityService;

    @Override
    public List<BorrowRecordResponseDto> getAll() {
        log.info("Fetching all borrow records");
        return borrowRecordRepository.findAll()
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowRecordResponseDto borrowBook(BorrowRecordRequestDto requestDto) {
        log.info("Processing borrow request: userId={}, bookId={}", requestDto.getUserId(), requestDto.getBookId());

        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", requestDto.getBookId());
                    return new ApiException("Book not found with id: " + requestDto.getBookId(), HttpStatus.NOT_FOUND);
                });

        if (!book.isAvailable()) {
            log.warn("Book with ID {} is not available for borrowing", requestDto.getBookId());
            throw new ApiException("Book is currently not available for borrowing", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", requestDto.getUserId());
                    return new ApiException("User not found with id: " + requestDto.getUserId(), HttpStatus.NOT_FOUND);
                });

        BorrowRecord record = BorrowRecordMapper.toEntity(requestDto, user, book);
        book.setAvailable(false);
        // Emit event to notify subscribers that the book has been borrowed (Reactive - WebFlux)
        bookAvailabilityService.publishAvailabilityChange(book.getId().toString(), false);

        bookRepository.save(book);
        borrowRecordRepository.save(record);

        log.info("Borrow record created successfully: recordId={}", record.getId());
        return BorrowRecordMapper.toResponseDto(record);
    }

    @Override
    public BorrowRecordResponseDto returnBook(UUID borrowRecordId) {
        log.info("Processing return for borrow record ID: {}", borrowRecordId);

        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> {
                    log.warn("Borrow record not found with ID: {}", borrowRecordId);
                    return new ApiException("Borrow record not found with id: " + borrowRecordId, HttpStatus.NOT_FOUND);
                });

        if (record.isReturned()) {
            log.warn("Borrow record with ID {} is already marked as returned", borrowRecordId);
            throw new ApiException("This book has already been returned", HttpStatus.BAD_REQUEST);
        }

        record.setReturned(true);
        record.setReturnDate(LocalDate.now());

        Book book = record.getBook();
        book.setAvailable(true);
        // Emit event to notify subscribers that the book has been returned and is now available (Reactive - WebFlux)
        bookAvailabilityService.publishAvailabilityChange(book.getId().toString(), true);

        bookRepository.save(book);
        borrowRecordRepository.save(record);

        log.info("Book returned successfully for record ID: {}", borrowRecordId);
        return BorrowRecordMapper.toResponseDto(record);
    }

    @Override
    public List<BorrowRecordResponseDto> getBorrowRecordsByUserId(UUID userId) {
        log.info("Fetching borrow records for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("User not found with ID: {}", userId);
            throw new ApiException("User not found with id: " + userId, HttpStatus.NOT_FOUND);
        }

        return borrowRecordRepository.findByUserId(userId)
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordResponseDto> getOwnBorrowRecords(String emailFromToken) {
        User user = userRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new ApiException("User not found with email: " + emailFromToken, HttpStatus.NOT_FOUND));

        log.info("Fetching borrow records for authenticated user ID: {}", user.getId());

        return borrowRecordRepository.findByUserId(user.getId())
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordResponseDto> getOverdueRecords() {
        log.info("Fetching overdue borrow records (not returned, due date before today)");
        return borrowRecordRepository.findByReturnedFalseAndDueDateBefore(LocalDate.now())
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}