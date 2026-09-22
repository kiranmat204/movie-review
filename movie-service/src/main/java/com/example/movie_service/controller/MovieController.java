package com.example.movie_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_service.exception.DuplicateMovieException;
import com.example.movie_service.exception.MovieNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {
    private final MovieRepository movieRepository;

    public MovieController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // This method gets all the movies
    @GetMapping
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @GetMapping("/{id}")
    public Movie getMovieById(@PathVariable Long id) {

        return movieRepository
                .findById(id)
                .orElseThrow(() ->
                        new MovieNotFoundException("Movie not found")
                );
    }

    // This method creates a new movie
    @PostMapping
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody Movie movie) {

        // checks for duplicate title and year entries
        if (movieRepository.existsByTitleIgnoreCaseAndReleaseYear(
                movie.getTitle(),
                movie.getReleaseYear())) {

            throw new DuplicateMovieException(
                    "This movie already exists"
            );
        }

        Movie savedMovie = movieRepository.save(movie);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedMovie);
    }

    // This method updates movie title/releaseYear/genre
    @PutMapping("/{id}")
    public Movie updateMovie(@PathVariable Long id, @Valid @RequestBody Movie updatedMovie) {

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() ->
                        new MovieNotFoundException("Movie not found")
                );

        movie.setTitle(updatedMovie.getTitle());
        movie.setReleaseYear(updatedMovie.getReleaseYear());
        movie.setGenre(updatedMovie.getGenre());
        movie.setPosterUrl(updatedMovie.getPosterUrl());
        movie.setDescription(updatedMovie.getDescription());

        return movieRepository.save(movie);
    }

    // This method deletes a movie by its ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() ->
                        new MovieNotFoundException("Movie not found")
                );

        movieRepository.delete(movie);

        return ResponseEntity.noContent().build();
    }
}
