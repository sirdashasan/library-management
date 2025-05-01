package com.hasan.library_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookAvailabilityEvent {
    private String bookId;
    private boolean available;
}