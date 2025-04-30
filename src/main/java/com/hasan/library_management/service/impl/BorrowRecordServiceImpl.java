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
import com.hasan.library_management.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    public List<BorrowRecordResponseDto> getAll() {
        return borrowRecordRepository.findAll()
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowRecordResponseDto borrowBook(BorrowRecordRequestDto requestDto) {
        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new ApiException("Book not found with id: " + requestDto.getBookId(), HttpStatus.NOT_FOUND));
        if (!book.isAvailable()) {
            throw new ApiException("Book is currently not available for borrowing", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ApiException("User not found with id: " + requestDto.getUserId(), HttpStatus.NOT_FOUND));

        BorrowRecord record = BorrowRecordMapper.toEntity(requestDto, user, book);
        book.setAvailable(false);
        bookRepository.save(book);
        borrowRecordRepository.save(record);

        return BorrowRecordMapper.toResponseDto(record);
    }

    @Override
    public BorrowRecordResponseDto returnBook(UUID borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ApiException("Borrow record not found with id: " + borrowRecordId, HttpStatus.NOT_FOUND));

        if (record.isReturned()) {
            throw new ApiException("This book has already been returned", HttpStatus.BAD_REQUEST);
        }

        record.setReturned(true);
        record.setReturnDate(LocalDate.now());

        Book book = record.getBook();
        book.setAvailable(true);

        bookRepository.save(book);
        borrowRecordRepository.save(record);

        return BorrowRecordMapper.toResponseDto(record);
    }

    @Override
    public List<BorrowRecordResponseDto> getBorrowRecordsByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("User not found with id: " + userId, HttpStatus.NOT_FOUND);
        }

        return borrowRecordRepository.findByUserId(userId)
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowRecordResponseDto> getOverdueRecords() {
        return borrowRecordRepository.findByReturnedFalseAndDueDateBefore(LocalDate.now())
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}