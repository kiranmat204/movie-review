package com.example.review_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.example.review_service.dto.MovieResponse;
import com.example.review_service.exception.MovieNotFoundException;
import com.example.review_service.exception.MovieServiceUnavailableException;

@Component
public class MovieClient {

    private final RestClient restClient;

    public MovieClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public MovieResponse getMovieById(Long movieId) {

        try {
            return restClient
                    .get()
                    .uri("/api/movies/{movieId}", movieId)
                    .retrieve()
                    .body(MovieResponse.class);

        } catch (HttpClientErrorException.NotFound exception) {
            throw new MovieNotFoundException(movieId);

        } catch (ResourceAccessException | HttpServerErrorException exception) {
            throw new MovieServiceUnavailableException();
        }
    }
}