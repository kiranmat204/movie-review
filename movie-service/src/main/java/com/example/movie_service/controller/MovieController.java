package com.example.movie_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_service.exception.DuplicateResourceException;
import com.example.movie_service.exception.ResourceNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;

@RestController
@RequestMapping("/api/movies")
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
                        new ResourceNotFoundException("Movie not found")
                );
    }

    // This method creates a new movie
    @PostMapping
    public Movie createMovie(@RequestBody Movie movie) {

        // checks for duplicate title and year entries
       if (movieRepository.existsByTitleIgnoreCaseAndReleaseYear(
            movie.getTitle(), 
            movie.getReleaseYear())) {

            throw new DuplicateResourceException(
                    "This movie already exists"
            );
        }

        return movieRepository.save(movie);
    }

    // This method updates movie title/releaseYear/genre
    @PutMapping("/{id}")
    public Movie updateMovie(@PathVariable Long id, @RequestBody Movie updatedMovie) {

        Movie movie = movieRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Movie not found")
                );

        movie.setTitle(updatedMovie.getTitle());
        movie.setReleaseYear(updatedMovie.getReleaseYear());
        movie.setGenre(updatedMovie.getGenre());

        return movieRepository.save(movie);
    }
}
