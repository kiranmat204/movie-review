package com.example.review_service.exception;

public class MovieServiceUnavailableException extends RuntimeException {

    public MovieServiceUnavailableException() {
        super("The movie service is currently unavailable");
    }
}