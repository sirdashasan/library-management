package com.hasan.library_management.service;


import com.hasan.library_management.dto.response.BookAvailabilityEvent;
import lombok.Getter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class BookAvailabilityService {

    private final Sinks.Many<BookAvailabilityEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Getter
    private final Flux<BookAvailabilityEvent> stream = sink.asFlux();

    public void publishAvailabilityChange(String bookId, boolean available) {
        sink.tryEmitNext(new BookAvailabilityEvent(bookId, available));
    }
}