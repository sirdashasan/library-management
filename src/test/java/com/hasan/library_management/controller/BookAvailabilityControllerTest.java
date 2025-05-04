package com.hasan.library_management.controller;

import com.hasan.library_management.dto.response.BookAvailabilityEvent;
import com.hasan.library_management.service.BookAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookAvailabilityControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BookAvailabilityService availabilityService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setupClient() {
        this.webTestClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void streamAvailability_shouldEmitEvents() {
        // Create a test event with a random book ID and availability = true
        BookAvailabilityEvent testEvent = new BookAvailabilityEvent(UUID.randomUUID().toString(), true);

        // Emit the event after a short delay to ensure the stream is already subscribed
        new Thread(() -> {
            try {
                Thread.sleep(500); // short delay to avoid event being missed
                availabilityService.publishAvailabilityChange(
                        testEvent.getBookId(), testEvent.isAvailable()
                );
            } catch (InterruptedException ignored) {}
        }).start();

        // Send a GET request to the event stream endpoint and capture the result
        var result = webTestClient.get()
                .uri("/library/api/books/availability-stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(BookAvailabilityEvent.class);

        // Take the first emitted event from the stream and verify its content
        var events = result.getResponseBody().take(1).collectList().block();

        // Assert that the emitted event matches the one we sent
        assertThat(events)
                .isNotNull()
                .isNotEmpty()
                .anyMatch(event ->
                        event.getBookId().equals(testEvent.getBookId()) &&
                                event.isAvailable() == testEvent.isAvailable());
    }

}