package com.example.movie_service.exception;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
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

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
                DataIntegrityViolationException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error",
                        "This movie already exists"
                ));
        }
        @ExceptionHandler(JpaSystemException.class)
        public ResponseEntity<Map<String, String>> handleJpaSystemException(
                JpaSystemException exception) {

        String causeMessage = exception
                .getMostSpecificCause()
                .getMessage();

        if (causeMessage != null
                && causeMessage.contains("SQLITE_CONSTRAINT_UNIQUE")) {

                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "error",
                                "This movie already exists"
                        ));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error",
                        "A database error occurred"
                ));
        }
}