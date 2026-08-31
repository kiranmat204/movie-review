package com.example.movie_review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.movie_review.model.Movie;

public interface  MovieRepository extends JpaRepository<Movie, Long>{
    
}
