package com.hasan.library_management.controller;


import com.hasan.library_management.dto.response.BookAvailabilityEvent;
import com.hasan.library_management.service.BookAvailabilityService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Hidden
@RestController
@RequiredArgsConstructor
public class BookAvailabilityController {

    private final BookAvailabilityService availabilityService;

    @GetMapping(value = "/books/availability-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BookAvailabilityEvent> streamAvailability() {
        System.out.println("Subscribed to stream on thread: " + Thread.currentThread().getName());
        return availabilityService.getStream()
                .doOnNext(event -> System.out.println("📡 Emitted: " + event + " on thread: " + Thread.currentThread().getName()));
    }
}
