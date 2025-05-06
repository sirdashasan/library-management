package com.hasan.library_management.config;

import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.BorrowRecord;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.repository.BorrowRecordRepository;
import com.hasan.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final BorrowRecordRepository borrowRecordRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Create librarian account if not exists
        User admin = userRepo.findByEmail("admin@gmail.com")
                .orElseGet(() -> userRepo.save(User.builder()
                        .name("Admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.LIBRARIAN)
                        .phoneNumber("5554214577")
                        .build()));

        // Create patron account if not exists
        User patron = userRepo.findByEmail("hasan@gmail.com")
                .orElseGet(() -> userRepo.save(User.builder()
                        .name("Hasan")
                        .email("hasan@gmail.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.PATRON)
                        .phoneNumber("5559876543")
                        .build()));

        // Create books if they don't already exist
        Book book1 = createBookIfMissing("9780451524932", "The Feeling Good Book", "Andrew Sharman", LocalDate.of(1980, 7, 1), "Personal Development");
        Book book2 = createBookIfMissing("9780743273565", "The Great Gatsby", "F. Scott Fitzgerald", LocalDate.of(1925, 4, 10), "Classic");
        Book book3 = createBookIfMissing("9780439023528", "The Hunger Games", "Suzanne Collins", LocalDate.of(2008, 9, 14), "Dystopian");
        createBookIfMissing("9780544003415", "The Lord of the Rings", "J.R.R. Tolkien", LocalDate.of(1954, 7, 29), "Fantasy");
        createBookIfMissing("9780307277671", "The Road", "Cormac McCarthy", LocalDate.of(2006, 9, 26), "Post-apocalyptic");

        // Create mock overdue borrow records
        createOverdueBorrowRecordIfNotExists(patron, book1);
        createOverdueBorrowRecordIfNotExists(patron, book2);
        createOverdueBorrowRecordIfNotExists(patron, book3);

        System.out.println("🟢 Mock data (users, books, and overdue borrow records) loaded successfully.");
    }

    private Book createBookIfMissing(String isbn, String title, String author, LocalDate pubDate, String genre) {
        return bookRepo.findByIsbn(isbn)
                .orElseGet(() -> bookRepo.save(Book.builder()
                        .title(title)
                        .author(author)
                        .isbn(isbn)
                        .publicationDate(pubDate)
                        .genre(genre)
                        .available(true)
                        .build()));
    }

    private void createOverdueBorrowRecordIfNotExists(User user, Book book) {
        boolean alreadyExists = borrowRecordRepo.findByBookId(book.getId()).stream()
                .anyMatch(record -> !record.isReturned());

        if (!alreadyExists) {
            book.setAvailable(false); // Mark book as borrowed
            bookRepo.save(book);

            BorrowRecord record = BorrowRecord.builder()
                    .book(book)
                    .user(user)
                    .borrowDate(LocalDate.now().minusDays(20))   // Borrowed 20 days ago
                    .dueDate(LocalDate.now().minusDays(10))      // Due 10 days ago
                    .returned(false)
                    .build();

            borrowRecordRepo.save(record);
        }
    }
}
