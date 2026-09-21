package com.example.movie_service.controller;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.movie_service.exception.DuplicateMovieException;
import com.example.movie_service.exception.MovieNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {
        @Mock
        private MovieRepository movieRepository;

        @InjectMocks
        private MovieController movieController;


        @Test
        @DisplayName("Creating a movie returns 201 Created")
        void createMovieReturnsCreatedMovie() {
                Movie movie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                Movie savedMovie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                savedMovie.setId(1L);

                when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear(
                        "Interstellar",
                        2014
                )).thenReturn(false);

                when(movieRepository.save(movie)).thenReturn(savedMovie);

                ResponseEntity<Movie> response =
                        movieController.createMovie(movie);

  
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertSame(savedMovie, response.getBody());

                verify(movieRepository).save(movie);
        }

        @Test
        @DisplayName("Creating a movie that exists throws DuplicateMovieException")
        void createMovieThrowsExceptionWhenMovieAlreadyExists() {
                Movie movie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                when(movieRepository.existsByTitleIgnoreCaseAndReleaseYear(
                        "Interstellar",
                        2014
                )).thenReturn(true);

                DuplicateMovieException exception = assertThrows(
                        DuplicateMovieException.class,
                        () -> movieController.createMovie(movie)
                );

                assertEquals(
                        "This movie already exists",
                        exception.getMessage()
                );

                verify(movieRepository, never()).save(movie);
        }

        @Test
        @DisplayName("Getting all movies returns 200 OK")
        void getAllMoviesReturnsAllMovies() {
                Movie firstMovie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                Movie secondMovie = new Movie(
                        "Arrival",
                        2016,
                        "Science Fiction"
                );

                List<Movie> movies = List.of(firstMovie, secondMovie);

                when(movieRepository.findAll()).thenReturn(movies);

                List<Movie> result = movieController.getAllMovies();

                assertEquals(2, result.size());
                assertSame(movies, result);

                verify(movieRepository).findAll();
        }

        @Test
        @DisplayName("Getting a movie by id returns 200 OK")
        void getMovieByIdReturnsMovie() {
                Movie movie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                movie.setId(1L);

                when(movieRepository.findById(1L))
                        .thenReturn(Optional.of(movie));

                Movie result = movieController.getMovieById(1L);

                assertSame(movie, result);
                assertEquals(1L, result.getId());

                verify(movieRepository).findById(1L);
        }

        @Test
        @DisplayName("Updating a movie returns 200 OK")
        void updateMovieReturnsUpdatedMovie() {
                Movie existingMovie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                existingMovie.setId(1L);

                Movie updatedDetails = new Movie(
                        "Interstellar",
                        2014,
                        "Sci-Fi"
                );

                when(movieRepository.findById(1L))
                        .thenReturn(Optional.of(existingMovie));

                when(movieRepository.save(existingMovie))
                        .thenReturn(existingMovie);

                Movie result = movieController.updateMovie(
                        1L,
                        updatedDetails
                );

                assertEquals("Interstellar", result.getTitle());
                assertEquals(2014, result.getReleaseYear());
                assertEquals("Sci-Fi", result.getGenre());

                verify(movieRepository).findById(1L);
                verify(movieRepository).save(existingMovie);
        }

        @Test
        @DisplayName("Getting all movies returns 204 No Content")
        void deleteMovieReturnsNoContent() {
                Movie movie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                movie.setId(1L);

                when(movieRepository.findById(1L))
                        .thenReturn(Optional.of(movie));

                ResponseEntity<Void> response =
                        movieController.deleteMovie(1L);

                assertEquals(
                        HttpStatus.NO_CONTENT,
                        response.getStatusCode()
                );

                verify(movieRepository).findById(1L);
                verify(movieRepository).delete(movie);
        }

        @Test
        @DisplayName("Getting a movie if it doesnt exist, throws MovieNotFoundException")
        void getMovieByIdThrowsExceptionWhenMovieDoesNotExist() {
                when(movieRepository.findById(999L))
                        .thenReturn(Optional.empty());

                MovieNotFoundException exception = assertThrows(
                        MovieNotFoundException.class,
                        () -> movieController.getMovieById(999L)
                );

                assertEquals("Movie not found", exception.getMessage());

                verify(movieRepository).findById(999L);
        }

        @Test
        @DisplayName("Updating a movie if it doesnt exist, throws MovieNotFoundException")
        void updateMovieThrowsExceptionWhenMovieDoesNotExist() {
                Movie updatedDetails = new Movie(
                        "Unknown Movie",
                        2026,
                        "Drama"
                );

                when(movieRepository.findById(999L))
                        .thenReturn(Optional.empty());

                MovieNotFoundException exception = assertThrows(
                        MovieNotFoundException.class,
                        () -> movieController.updateMovie(
                                999L,
                                updatedDetails
                        )
                );

                assertEquals("Movie not found", exception.getMessage());

                verify(movieRepository).findById(999L);
                verify(movieRepository, never()).save(any(Movie.class));
        }

        @Test
        @DisplayName("Deleting a movie if it doesnt exist, throws MovieNotFoundException")
        void deleteMovieThrowsExceptionWhenMovieDoesNotExist() {
                when(movieRepository.findById(999L))
                        .thenReturn(Optional.empty());

                MovieNotFoundException exception = assertThrows(
                        MovieNotFoundException.class,
                        () -> movieController.deleteMovie(999L)
                );

                assertEquals("Movie not found", exception.getMessage());

                verify(movieRepository).findById(999L);
                verify(movieRepository, never()).delete(any(Movie.class));
        }
}