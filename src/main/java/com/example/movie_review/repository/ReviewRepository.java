package com.example.movie_review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.movie_review.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieId(Long movieId);
}
