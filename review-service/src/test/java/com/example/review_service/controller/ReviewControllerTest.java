package com.example.review_service.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.review_service.client.MovieClient;
import com.example.review_service.exception.MovieNotFoundException;
import com.example.review_service.exception.ReviewNotFoundException;
import com.example.review_service.model.Review;
import com.example.review_service.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieClient movieClient;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUpRequestContext() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8082);
        request.setRequestURI("/api/movies/1/reviews");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Getting reviews of a movie returns 200 OK")
    void getReviewsByMovieReturnsReviews() {

        Review firstReview = new Review(
                "Kiran",
                5,
                "Great movie",
                1L
        );

        Review secondReview = new Review(
                "Alice",
                4,
                "Very enjoyable",
                1L
        );

        List<Review> reviews = List.of(
                firstReview,
                secondReview
        );

        when(reviewRepository.findByMovieId(1L))
                .thenReturn(reviews);

        List<Review> result =
                reviewController.getReviewsByMovie(1L);

        assertEquals(2, result.size());
        assertSame(reviews, result);

        verify(movieClient).getMovieById(1L);
        verify(reviewRepository).findByMovieId(1L);
    }

    @Test
    @DisplayName("Creating a review for a movie returns 201 Created")
    void createReviewReturnsCreatedReview() {

        Review review = new Review(
                "Kiran",
                5,
                "Great movie",
                null
        );

        Review savedReview = new Review(
                "Kiran",
                5,
                "Great movie",
                1L
        );

        savedReview.setId(1L);

        when(reviewRepository.save(review))
                .thenReturn(savedReview);

        ResponseEntity<Review> response =
                reviewController.createReview(1L, review);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertSame(savedReview, response.getBody());
        assertEquals(1L, review.getMovieId());

        assertEquals(
                "http://localhost:8082/api/movies/1/reviews/1",
                response.getHeaders().getLocation().toString()
        );

        verify(movieClient).getMovieById(1L);
        verify(reviewRepository).save(review);
    }

    @Test
    @DisplayName("Deleting a review for a movie returns 204 No Content")
    void deleteReviewReturnsNoContent() {

        Review review = new Review(
                "Kiran",
                5,
                "Great movie",
                1L
        );

        review.setId(1L);

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        ResponseEntity<Void> response =
                reviewController.deleteReview(1L, 1L);

        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );

        verify(movieClient).getMovieById(1L);
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("Deleting a review for a movie when it doesn't exist throws ReviewNotFoundException")
    void deleteReviewThrowsExceptionWhenReviewDoesNotExist() {

        when(reviewRepository.findById(999L))
                .thenReturn(Optional.empty());

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewController.deleteReview(1L, 999L)
        );

        assertEquals(
                "Review with ID 999 was not found",
                exception.getMessage()
        );

        verify(movieClient).getMovieById(1L);
        verify(reviewRepository).findById(999L);
        verify(reviewRepository, never()).delete(
                org.mockito.ArgumentMatchers.any(Review.class)
        );
    }

    @Test
    @DisplayName("Deleting a review belongning to a different movie throws ReviewNotFoundException")
    void deleteReviewThrowsExceptionWhenReviewBelongsToAnotherMovie() {

        Review review = new Review(
                "Kiran",
                5,
                "Great movie",
                2L
        );

        review.setId(1L);

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        ReviewNotFoundException exception = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewController.deleteReview(1L, 1L)
        );

        assertEquals(
                "Review with ID 1 was not found for movie with ID 1",
                exception.getMessage()
        );

        verify(movieClient).getMovieById(1L);
        verify(reviewRepository).findById(1L);
        verify(reviewRepository, never()).delete(review);
    }

    @Test
    @DisplayName("Deleting a review belongning to a different movie throws ReviewNotFoundException")
    void getReviewsDoesNotAccessRepositoryWhenMovieDoesNotExist() {

        doThrow(new MovieNotFoundException(999L))
                .when(movieClient)
                .getMovieById(999L);

        MovieNotFoundException exception = assertThrows(
                MovieNotFoundException.class,
                () -> reviewController.getReviewsByMovie(999L)
        );

        assertEquals(
                "Movie with ID 999 was not found",
                exception.getMessage()
        );

        verify(movieClient).getMovieById(999L);

        verify(reviewRepository, never())
                .findByMovieId(999L);
    }
}