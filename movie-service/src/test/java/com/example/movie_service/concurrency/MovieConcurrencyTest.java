package com.example.movie_service.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.example.movie_service.MovieServiceApplication;
import com.example.movie_service.model.Movie;
import com.example.movie_service.repository.MovieRepository;

@DisplayName("Movie service concurrency tests")
@SpringBootTest(
        classes = MovieServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class MovieConcurrencyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MovieRepository movieRepository;

    private RestClient restClient;


    @BeforeEach
    void setUp() {

        movieRepository.deleteAllInBatch();

        restClient = RestClient
                .builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    /*
        Verifies that the movie service can process multiple read
        requests concurrently without returning an error
    */
    @Test
    @DisplayName("Concurrent movie reads all return 200 OK")
    void concurrentReadsAllReturnOk() throws Exception {
        movieRepository.save(
                new Movie(
                        "Interstellar",
                        2014,
                        "Science Fiction"
                )
        );

        List<Callable<Integer>> requests = new ArrayList<>();

        for (int index = 0; index < 10; index++) {
            requests.add(() -> getMoviesStatus());
        }

        List<Integer> statuses =
                runConcurrently(requests);

        long successfulRequests = statuses
                .stream()
                .filter(status -> status == 200)
                .count();

        assertEquals(10, successfulRequests);
    }

    /*
        Verifies that multiple different movies can be created
        concurrently and that every request returns 201 Created.
    */
    @Test
    @DisplayName("Concurrent unique movie writes all return 201 Created")
    void concurrentUniqueWritesAllReturnCreated() throws Exception {
        List<Callable<Integer>> requests = new ArrayList<>();

        for (int index = 0; index < 5; index++) {

            int movieNumber = index;

            requests.add(() -> createMovieStatus(
                    new Movie(
                            "Movie " + movieNumber,
                            2000 + movieNumber,
                            "Drama"
                    )
            ));
        }

        List<Integer> statuses =
                runConcurrently(requests);

        long createdResponses = statuses
                .stream()
                .filter(status -> status == 201)
                .count();

        assertEquals(5, createdResponses);
        assertEquals(5, movieRepository.count());
    }

    /*
        Verifies that simultaneous requests for the same movie
        create only one record and reject the remaining requests.
    */
    @Test
    @DisplayName("Concurrent duplicate writes create one movie and return conflicts")
    void concurrentDuplicateWritesCreateOnlyOneMovie()
            throws Exception {

        List<Callable<Integer>> requests = new ArrayList<>();

        for (int index = 0; index < 5; index++) {

            requests.add(() -> createMovieStatus(
                    new Movie(
                            "Interstellar",
                            2014,
                            "Science Fiction"
                    )
            ));
        }

        List<Integer> statuses =
                runConcurrently(requests);

        long createdResponses = statuses
                .stream()
                .filter(status -> status == 201)
                .count();

        long conflictResponses = statuses
                .stream()
                .filter(status -> status == 409)
                .count();

        assertEquals(1, createdResponses);
        assertEquals(4, conflictResponses);
        assertEquals(1, movieRepository.count());
    }

    // Sends a GET request to retrieve all movies, then returns the HTTP status code
    private int getMoviesStatus() {

        return restClient
                .get()
                .uri("/api/movies")
                .exchange((request, response) ->
                        response.getStatusCode().value()
                );
    }

    // Send a POST request to create a movie, then returns the HTTP status code
    private int createMovieStatus(Movie movie) {

        return restClient
                .post()
                .uri("/api/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .body(movie)
                .exchange((request, response) ->
                        response.getStatusCode().value()
                );
    }

    /*
        Holds each request until every task is ready, 
        then releases them together to simulate concurrent clients.
    */
    private List<Integer> runConcurrently(List<Callable<Integer>> requests) throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(requests.size());

        CountDownLatch readyLatch =
                new CountDownLatch(requests.size());

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<Integer>> futures =
                new ArrayList<>();

        try {
            for (Callable<Integer> request : requests) {

                futures.add(executor.submit(() -> {

                    readyLatch.countDown();
                    startLatch.await();

                    return request.call();
                }));
            }

            // Wait until every task is ready
            readyLatch.await(5, TimeUnit.SECONDS);

            // Release all tasks at approximately the same time
            startLatch.countDown();

            List<Integer> statuses = new ArrayList<>();

            for (Future<Integer> future : futures) {
                statuses.add(
                        future.get(15, TimeUnit.SECONDS)
                );
            }

            return statuses;

        } finally {
            executor.shutdownNow();
        }
    }
}