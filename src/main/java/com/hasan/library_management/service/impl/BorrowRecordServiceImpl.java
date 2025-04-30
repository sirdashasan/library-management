package com.hasan.library_management.service.impl;

import com.hasan.library_management.dto.request.BorrowRequestDto;
import com.hasan.library_management.dto.response.BorrowResponseDto;
import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.mapper.BorrowRecordMapper;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.repository.BorrowRecordRepository;
import com.hasan.library_management.repository.UserRepository;
import com.hasan.library_management.service.BorrowRecordService;
import lombok.RequiredArgsConstructor;
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
    public List<BorrowResponseDto> getAll() {
        return borrowRecordRepository.findAll()
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowResponseDto borrowBook(BorrowRequestDto requestDto) {
        Book book = bookRepository.findById(requestDto.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available");
        }

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BorrowRecord record = BorrowRecordMapper.toEntity(requestDto, user, book);
        book.setAvailable(false);
        bookRepository.save(book);
        borrowRecordRepository.save(record);

        return BorrowRecordMapper.toResponseDto(record);
    }

    @Override
    public BorrowResponseDto returnBook(UUID borrowRecordId) {
        BorrowRecord record = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (record.isReturned()) {
            throw new RuntimeException("Book already returned");
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
    public List<BorrowResponseDto> getBorrowRecordsByUserId(UUID userId) {
        return borrowRecordRepository.findByUserId(userId)
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponseDto> getOverdueRecords() {
        return borrowRecordRepository.findByReturnedFalseAndDueDateBefore(LocalDate.now())
                .stream()
                .map(BorrowRecordMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}