package com.example.review_service.dto;

public record MovieResponse(
        Long id,
        String title,
        int releaseYear,
        String genre
) {
}
