package com.hasan.library_management.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponseDto {

    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private LocalDate publicationDate;
    private String genre;
    private boolean available;
}