package com.example.review_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.review_service.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovieId(Long movieId);
}