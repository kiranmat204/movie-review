package com.example.review_service.dto;

public record ApiError(
        String code,
        String message,
        String path
) {
}