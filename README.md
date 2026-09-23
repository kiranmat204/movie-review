# Movie Review Application

A distributed movie review application built with Spring Boot and React.

The project contains:

- `movie-service` – manages movies
- `review-service` – manages reviews and communicates with movie-service
- `frontend` – React user interface

# Main Features

- Create, view, edit, and delete movies
- Movie validation and duplicate movie protection
- Add, view, filter, and delete reviews
- Movie details page with poster URL and description
- Friendly validation and service-unavailable error messages
- Separate movie and review services with separate databases

## Requirements

- Java 21
- Node.js and npm
- Git

SQLite is included through the Spring Boot dependencies, so no separate database installation is needed.

## Setup and Run

Start the services in this order.

### 1. Movie service

```bash
cd movie-service
./mvnw spring-boot:run
```

- Runs on http://localhost:8081

### 2. Review service

- Open a new terminal

```bash
cd review-service
./mvnw spring-boot:run
```

- Runs on http://localhost:8082

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

- Open the URL shown by Vite
- OR copy this URL onto a browser: http://localhost:5173

# Database Config

Each service uses its own SQLite database file:

- movie-service/movie-review.db
- review-service/review-service.db

The database files are created automatically when the services starts.

- review-service uses this optional environment variable: MOVIE_SERVICE_URL
- If it is not provided, it defaults to: http://localhost:8081

# Testing 

### 1. Run movie-service tests:

```bash
cd movie-service
./mvnw test
```

### 2. Run review-service tests:

```bash
cd review-service
./mvnw test
```

### Additonally, Manual testing can be completed through the frontend, for example:

- Create and edit a movie
- Add a review with a rating
- Filter reviews by rating
- Delete a review
- Delete a movie
- Stop either service and check the service-unavailable message

# Known limitations

- Poster images use external URLs. Some image hosts may block browser access, so the placeholder image may appear instead.
- Deleting a movie does not currently delete its separately stored reviews from review-service.