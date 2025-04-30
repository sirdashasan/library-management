package com.hasan.library_management.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequestDto {

    private UUID userId;
    private UUID bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
}