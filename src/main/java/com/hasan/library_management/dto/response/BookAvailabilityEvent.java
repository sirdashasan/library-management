package com.hasan.library_management.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(hidden = true)
@Data
@AllArgsConstructor
public class BookAvailabilityEvent {
    private String bookId;
    private boolean available;
}