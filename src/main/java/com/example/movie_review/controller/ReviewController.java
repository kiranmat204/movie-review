package com.example.movie_review.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_review.exception.ResourceNotFoundException;
import com.example.movie_review.model.Movie;
import com.example.movie_review.model.Review;
import com.example.movie_review.repository.MovieRepository;
import com.example.movie_review.repository.ReviewRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movies")
public class ReviewController {
    
    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    public ReviewController(ReviewRepository reviewRepository, MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.movieRepository = movieRepository;
    }

    // This method gets the reviews for a specifc movie
    @GetMapping("/{movieId}/reviews")
    public List<Review> getReviewsByMovie(@PathVariable Long movieId) {

        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Movie not found")
                );

        return reviewRepository.findByMovieId(movie.getId());
    }

    // This method creates a review for a specific movie
    @PostMapping("/{movieId}/reviews")
    public Review createReview(@PathVariable Long movieId, @Valid @RequestBody Review review) {

       Movie movie = movieRepository
        .findById(movieId)
        .orElseThrow(() ->
                new ResourceNotFoundException("Movie not found")
        );

        review.setMovie(movie);

        return reviewRepository.save(review);
    }

    // This method deletes a specific review for a specific movie
    @DeleteMapping("/{movieId}/reviews/{reviewId}")
    public void deleteReview(@PathVariable Long movieId, @PathVariable Long reviewId) {
        
        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Movie not found")
                );

        Review review = reviewRepository
                .findById(reviewId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Review not found")
                );

        if (!review.getMovie().getId().equals(movie.getId())) {
            throw new ResourceNotFoundException(
                    "Review not found for this movie"
            );
        }

        reviewRepository.delete(review);
    }
}