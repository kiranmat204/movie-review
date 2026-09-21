package com.example.movie_service.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(MovieNotFoundException.class)
        public ResponseEntity<Map<String, String>> handleResourceNotFound(
                MovieNotFoundException exception) {

                Map<String, String> error = Map.of(
                        "error", exception.getMessage()
                );

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(error);
        }

        @ExceptionHandler(DuplicateMovieException.class)
        public ResponseEntity<Map<String, String>> handleDuplicateResource(DuplicateMovieException exception) {
                Map<String, String> error = Map.of(
                        "error", exception.getMessage()
                );

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(error);
        }
}