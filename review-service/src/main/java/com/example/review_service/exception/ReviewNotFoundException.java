package com.example.review_service.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long reviewId) {
        super("Review with ID " + reviewId + " was not found");
    }

    public ReviewNotFoundException(Long reviewId, Long movieId) {
        super(
                "Review with ID " + reviewId
                + " was not found for movie with ID " + movieId
        );
    }
}
