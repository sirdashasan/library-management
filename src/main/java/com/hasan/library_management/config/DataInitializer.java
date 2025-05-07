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
        Book book4 = createBookIfMissing("9780544003415", "The Lord of the Rings", "J.R.R. Tolkien", LocalDate.of(1954, 7, 29), "Fantasy");
        Book book5 = createBookIfMissing("9780307277671", "The Road", "Cormac McCarthy", LocalDate.of(2006, 9, 26), "Post-apocalyptic");
        Book book6 = createBookIfMissing("9780061120084", "To Kill a Mockingbird", "Harper Lee", LocalDate.of(1960, 7, 11), "Classic");
        Book book7 = createBookIfMissing("9780141439600", "Pride and Prejudice", "Jane Austen", LocalDate.of(1813, 1, 28), "Romance");
        Book book8 = createBookIfMissing("9780307474278", "1984", "George Orwell", LocalDate.of(1949, 6, 8), "Dystopian");
        Book book9 = createBookIfMissing("9781451673319", "Fahrenheit 451", "Ray Bradbury", LocalDate.of(1953, 10, 19), "Science Fiction");
        Book book10 = createBookIfMissing("9780060850524", "Brave New World", "Aldous Huxley", LocalDate.of(1932, 9, 1), "Science Fiction");

        // Create mock overdue borrow records
        createOverdueBorrowRecordIfNotExists(patron, book1);


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
            book.setAvailable(false);
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
