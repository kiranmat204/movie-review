package com.example.review_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.review_service.client.MovieClient;
import com.example.review_service.exception.ReviewNotFoundException;
import com.example.review_service.model.Review;
import com.example.review_service.repository.ReviewRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final MovieClient movieClient;

    public ReviewController(
            ReviewRepository reviewRepository,
            MovieClient movieClient) {

        this.reviewRepository = reviewRepository;
        this.movieClient = movieClient;
    }

    // This method gets the reviews for a specific movie
    @GetMapping("/{movieId}/reviews")
    public List<Review> getReviewsByMovie(@PathVariable Long movieId) {

        movieClient.getMovieById(movieId);

        return reviewRepository.findByMovieId(movieId);
    }

    // This method creates a review for a specific movie
    @PostMapping("/{movieId}/reviews")
    public ResponseEntity<Review> createReview(
            @PathVariable Long movieId,
            @Valid @RequestBody Review review) {

        movieClient.getMovieById(movieId);

        review.setId(null);
        review.setMovieId(movieId);

        Review savedReview = reviewRepository.save(review);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{reviewId}")
                .buildAndExpand(savedReview.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(savedReview);
    }

    // This method deletes a specific review for a specific movie
    @DeleteMapping("/{movieId}/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long movieId,
            @PathVariable Long reviewId) {

        movieClient.getMovieById(movieId);

        Review review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId)
                );

        if (!review.getMovieId().equals(movieId)) {
            throw new ReviewNotFoundException(reviewId, movieId);
        }

        reviewRepository.delete(review);

        return ResponseEntity.noContent().build();
    }
}