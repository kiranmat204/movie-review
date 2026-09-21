package com.example.review_service.exception;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long movieId) {
        super("Movie with ID " + movieId + " was not found");
    }
}
