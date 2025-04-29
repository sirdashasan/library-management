package com.hasan.library_management.dto.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequestDto {

    private String title;
    private String author;
    private String isbn;
    private LocalDate publicationDate;
    private String genre;
}
