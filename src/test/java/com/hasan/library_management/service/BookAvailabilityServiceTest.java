package com.hasan.library_management.service;

import com.hasan.library_management.dto.response.BookAvailabilityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class BookAvailabilityServiceTest {

    // *** publishAvailabilityChange Tests ***
    @Test
    void publishAvailabilityChange_shouldEmitEvent() {
        // Arrange
        BookAvailabilityService service = new BookAvailabilityService();

        // Act & Assert
        StepVerifier.create(service.getStream())
                .then(() -> service.publishAvailabilityChange("book-123", true))
                .expectNextMatches(event ->
                        event.getBookId().equals("book-123") &&
                                event.isAvailable()
                )
                .thenCancel() // Cancel since this is an infinite stream
                .verify();
    }
}