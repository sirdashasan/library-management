package com.hasan.library_management.repository;

import com.hasan.library_management.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, UUID> {

    // Borrowing history of a specific user
    List<BorrowRecord> findByUserId(UUID userId);

    // All borrow records of a specific book
    List<BorrowRecord> findByBookId(UUID bookId);

    // Records that have not been returned yet
    List<BorrowRecord> findByReturnedFalse();

    // Overdue records (not returned and past due date)
    List<BorrowRecord> findByReturnedFalseAndDueDateBefore(LocalDate today);

    // Returns the number of active (unreturned) books
    int countByUserIdAndReturnedFalse(UUID userId);

    // Are there any overdue books (dueDate < today and not returned)
    boolean existsByUserIdAndReturnedFalseAndDueDateBefore(UUID userId, LocalDate today);
}