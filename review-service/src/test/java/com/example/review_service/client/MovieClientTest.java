package com.example.review_service.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.test.web.client.ExpectedCount.once;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.web.client.RestClient;

import com.example.review_service.dto.MovieResponse;
import com.example.review_service.exception.MovieNotFoundException;
import com.example.review_service.exception.MovieServiceUnavailableException;

class MovieClientTest {

    private MockRestServiceServer mockServer;
    private MovieClient movieClient;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient
                .builder()
                .baseUrl("http://localhost:8081");

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        movieClient = new MovieClient(builder.build());
    }

    @Test
    @DisplayName("Getting a movie returns 200 OK")
    void getMovieByIdReturnsMovieWhenMovieExists() {

        String responseBody = """
                {
                  "id": 1,
                  "title": "Interstellar",
                  "releaseYear": 2014,
                  "genre": "Science Fiction"
                }
                """;

        mockServer.expect(
                once(),
                requestTo("http://localhost:8081/api/movies/1")
        )
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(
                responseBody,
                MediaType.APPLICATION_JSON
        ));

        MovieResponse result = movieClient.getMovieById(1L);

        assertEquals(1L, result.id());
        assertEquals("Interstellar", result.title());
        assertEquals(2014, result.releaseYear());
        assertEquals("Science Fiction", result.genre());

        mockServer.verify();
    }

    @Test
    @DisplayName("Getting a movie that doesn't exist returns MovieNotFoundException")
    void getMovieByIdThrowsExceptionWhenMovieDoesNotExist() {

        mockServer.expect(
                once(),
                requestTo("http://localhost:8081/api/movies/999")
        )
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

        MovieNotFoundException exception = assertThrows(
                MovieNotFoundException.class,
                () -> movieClient.getMovieById(999L)
        );

        assertEquals(
                "Movie with ID 999 was not found",
                exception.getMessage()
        );

        mockServer.verify();
    }

    @Test
    @DisplayName("Getting a movie when movie server is not running")
    void getMovieByIdThrowsExceptionWhenMovieServiceFails() {

        mockServer.expect(
                once(),
                requestTo("http://localhost:8081/api/movies/1")
        )
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(
                HttpStatus.INTERNAL_SERVER_ERROR
        ));

        MovieServiceUnavailableException exception = assertThrows(
                MovieServiceUnavailableException.class,
                () -> movieClient.getMovieById(1L)
        );

        assertEquals(
                "The movie service is currently unavailable",
                exception.getMessage()
        );

        mockServer.verify();
    }
}