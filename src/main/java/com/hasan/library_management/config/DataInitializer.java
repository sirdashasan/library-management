package com.hasan.library_management.config;

import com.hasan.library_management.entity.Book;
import com.hasan.library_management.entity.Role;
import com.hasan.library_management.entity.User;
import com.hasan.library_management.repository.BookRepository;
import com.hasan.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // add admin user
        if (userRepo.findByEmail("admin@gmail.com").isEmpty()) {
            userRepo.save(User.builder()
                    .name("Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.LIBRARIAN)
                    .phoneNumber("5554214577")
                    .build());
        }

        // add patron user
        if (userRepo.findByEmail("hasan@gmail.com").isEmpty()) {
            userRepo.save(User.builder()
                    .name("Hasan")
                    .email("hasan@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.PATRON)
                    .phoneNumber("5559876543")
                    .build());
        }

        if (bookRepo.findByIsbn("9780451524932").isEmpty()) {
            bookRepo.save(Book.builder()
                    .title("The Feeling Good Book")
                    .author("Andrew Sharman")
                    .isbn("9780451524932")
                    .publicationDate(LocalDate.of(1980, 7, 1))
                    .genre("Personal Development")
                    .available(true)
                    .build());
        }

        if (bookRepo.findByIsbn("9780743273565").isEmpty()) {
            bookRepo.save(Book.builder()
                    .title("The Great Gatsby")
                    .author("F. Scott Fitzgerald")
                    .isbn("9780743273565")
                    .publicationDate(LocalDate.of(1925, 4, 10))
                    .genre("Classic")
                    .available(true)
                    .build());
        }

        if (bookRepo.findByIsbn("9780439023528").isEmpty()) {
            bookRepo.save(Book.builder()
                    .title("The Hunger Games")
                    .author("Suzanne Collins")
                    .isbn("9780439023528")
                    .publicationDate(LocalDate.of(2008, 9, 14))
                    .genre("Dystopian")
                    .available(true)
                    .build());
        }

        if (bookRepo.findByIsbn("9780544003415").isEmpty()) {
            bookRepo.save(Book.builder()
                    .title("The Lord of the Rings")
                    .author("J.R.R. Tolkien")
                    .isbn("9780544003415")
                    .publicationDate(LocalDate.of(1954, 7, 29))
                    .genre("Fantasy")
                    .available(true)
                    .build());
        }

        if (bookRepo.findByIsbn("9780307277671").isEmpty()) {
            bookRepo.save(Book.builder()
                    .title("The Road")
                    .author("Cormac McCarthy")
                    .isbn("9780307277671")
                    .publicationDate(LocalDate.of(2006, 9, 26))
                    .genre("Post-apocalyptic")
                    .available(true)
                    .build());
        }

        System.out.println("🟢 Mock data loaded successfully.");
    }

}
