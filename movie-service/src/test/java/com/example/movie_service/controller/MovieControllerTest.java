package com.example.movie_service.controller;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.movie_service.exception.DuplicateMovieException;
import com.example.movie_service.exception.GlobalExceptionHandler;
import com.example.movie_service.exception.MovieNotFoundException;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {
        @Mock
        private MovieRepository movieRepository;

        @InjectMocks
        private MovieController movieController;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
        movieRepository = mock(MovieRepository.class);
        movieController = new MovieController(movieRepository);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(movieController)
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
        }

        @Test
        @DisplayName("Creating a movie returns 201 Created with all movie fields")
        void createMovieReturnsCreatedMovie() {
                // Arrange
                Movie movie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                movie.setPosterUrl(
                        "https://example.com/interstellar.jpg"
                );

                movie.setDescription(
                        "A team travels through a wormhole to find a new home for humanity."
                );

                Movie savedMovie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                savedMovie.setId(1L);
                savedMovie.setPosterUrl(
                        "https://example.com/interstellar.jpg"
                );

                savedMovie.setDescription(
                        "A team travels through a wormhole to find a new home for humanity."
                );

                when(movieRepository
                        .existsByTitleIgnoreCaseAndReleaseYear(
                                "Interstellar",
                                2014
                        ))
                        .thenReturn(false);

                when(movieRepository.save(movie))
                        .thenReturn(savedMovie);

                // Act
                ResponseEntity<Movie> response =
                        movieController.createMovie(movie);

                // Assert
                assertEquals(
                        HttpStatus.CREATED,
                        response.getStatusCode()
                );

                assertSame(savedMovie, response.getBody());

                Movie responseBody = response.getBody();

                assertNotNull(responseBody);
                assertEquals(1L, responseBody.getId());
                assertEquals(
                        "Interstellar",
                        responseBody.getTitle()
                );

                assertEquals(
                        2014,
                        responseBody.getReleaseYear()
                );

                assertEquals(
                        "Science Fiction",
                        responseBody.getGenre()
                );

                assertEquals(
                        "https://example.com/interstellar.jpg",
                        responseBody.getPosterUrl()
                );

                assertEquals(
                        "A team travels through a wormhole to find a new home for humanity.",
                        responseBody.getDescription()
                );

                verify(movieRepository)
                        .existsByTitleIgnoreCaseAndReleaseYear(
                                "Interstellar",
                                2014
                        );

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

        // Checks that all editable movie fields are updated
        @Test
        @DisplayName("PUT updates all movie fields")
        void updateMovieUpdatesAllFields() {
                Movie existingMovie = new Movie(
                        "Old Title",
                        2000,
                        "Drama"
                );

                existingMovie.setId(1L);

                Movie updatedMovie = new Movie(
                        "New Title",
                        2020,
                        "Science Fiction"
                );

                updatedMovie.setPosterUrl(
                        "https://example.com/poster.jpg"
                );

                updatedMovie.setDescription(
                        "An updated movie description."
                );

                when(movieRepository.findById(1L))
                        .thenReturn(Optional.of(existingMovie));

                when(movieRepository.save(existingMovie))
                        .thenReturn(existingMovie);

                Movie result = movieController.updateMovie(
                        1L,
                        updatedMovie
                );

                assertEquals("New Title", result.getTitle());
                assertEquals(2020, result.getReleaseYear());
                assertEquals("Science Fiction", result.getGenre());

                assertEquals(
                        "https://example.com/poster.jpg",
                        result.getPosterUrl()
                );

                assertEquals(
                        "An updated movie description.",
                        result.getDescription()
                );

                verify(movieRepository).findById(1L);
                verify(movieRepository).save(existingMovie);
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

        @Test
        @DisplayName("POST returns 400 when release year is greater than 2100")
        void createMovieReturnsBadRequestForFutureReleaseYear() throws Exception {
                Movie movie = new Movie(
                        "Future Movie",
                        2200,
                        "Science Fiction"
                );

                String requestBody =
                        objectMapper.writeValueAsString(movie);

                mockMvc.perform(
                                post("/api/movies")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(
                                jsonPath("$.error")
                                        .value("Validation failed")
                        )
                        .andExpect(
                                jsonPath("$.fields.releaseYear")
                                        .value(
                                                "Release year cannot be later than 2100"
                                        )
                        );

                verifyNoInteractions(movieRepository);
        }

        @Test
        @DisplayName("POST returns 400 when movie title is blank")
        void createMovieReturnsBadRequestForBlankTitle() throws Exception {
                Movie movie = new Movie(
                        "",
                        2014,
                        "Science Fiction"
                );

                String requestBody =
                        objectMapper.writeValueAsString(movie);

                mockMvc.perform(
                                post("/api/movies")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(
                                jsonPath("$.fields.title")
                                        .value("Movie title is required")
                        );

                verifyNoInteractions(movieRepository);
        }

        @Test
        @DisplayName("POST returns 400 when description exceeds 1000 characters")
        void createMovieReturnsBadRequestForLongDescription() throws Exception {

                Movie movie = new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                );

                movie.setDescription("a".repeat(1001));

                String requestBody =
                        objectMapper.writeValueAsString(movie);

                mockMvc.perform(
                                post("/api/movies")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody)
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(
                                jsonPath("$.fields.description")
                                        .value(
                                                "Description cannot exceed 1000 characters"
                                        )
                        );

                verifyNoInteractions(movieRepository);
        }
}