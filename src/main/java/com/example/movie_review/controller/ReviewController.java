package com.example.movie_review.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/{movieId}/reviews")
    public Review createReview(@PathVariable Long movieId, @Valid @RequestBody Review review) {

        Movie movie = movieRepository.findById(movieId).orElseThrow();

        review.setMovie(movie);

        return reviewRepository.save(review);
    }
}