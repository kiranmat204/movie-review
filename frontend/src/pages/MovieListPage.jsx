import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import CreateMovieForm from "../components/CreateMovieForm";
import "../App.css";

const MOVIE_API_URL = "http://localhost:8081/api/movies";

function MovieListPage() {
    const navigate = useNavigate();

    const [movies, setMovies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [view, setView] = useState("list");

    // Loads all movies from the movie service
    async function loadMovies() {
    try {
        setLoading(true);
        setError("");

        const response = await fetch(MOVIE_API_URL);

        if (!response.ok) {
        throw new Error("Unable to load movies");
        }

        const data = await response.json();
        setMovies(data);
    } catch (exception) {
        setError(exception.message);
    } finally {
        setLoading(false);
    }
    }

    // Returns to the movie list and reloads the movies
    async function handleRefresh() {
    setView("list");
    await loadMovies();
    }

    // Reloads the list after a movie has been created
    async function handleMovieCreated() {
        await loadMovies();
        setView("list");
    }

    useEffect(() => {
    loadMovies();
    }, []);

    return (
    <div className="app">
        <header className="app-header">
        <div>
            <p className="eyebrow">Reviewflix</p>
            <h1>Movie Review</h1>
            <p className="subtitle">
            Browse movies and share your reviews.
            </p>
        </div>
        </header>

        <main>
        <section>
            <div className="section-header">
            <div>
                <h2>
                {view === "create"
                    ? "Create a new movie"
                    : "Movies"}
                </h2>

                {view === "list" && (
                <p>{movies.length} movies available</p>
                )}
            </div>

            <div className="section-actions">
                <button
                type="button"
                className="icon-button"
                onClick={() => setView("list")}
                title="Show movie list"
                aria-label="Show movie list"
                >
                <span className="list-icon">☰</span>
                </button>

                <button
                type="button"
                className="icon-button"
                onClick={handleRefresh}
                title="Refresh movies"
                aria-label="Refresh movies"
                >
                <span className="refresh-icon">↻</span>
                </button>

                <button
                type="button"
                className="icon-button add-icon-button"
                onClick={() => setView("create")}
                title="Add a movie"
                aria-label="Add a movie"
                >
                <span>+</span>
                </button>
            </div>
            </div>

            {view === "create" ? (
            <CreateMovieForm
                onCreated={handleMovieCreated}
                onCancel={() => setView("list")}
            />
            ) :  (
            <>
                {loading && (
                <p className="status-message">
                    Loading movies...
                </p>
                )}

                {!loading && error && (
                <div className="error-message">{error}</div>
                )}

                {!loading && !error && movies.length === 0 && (
                <div className="empty-state">
                    <h3>No movies yet</h3>
                    <p>Add the first movie to get started.</p>
                </div>
                )}

                {!loading && !error && movies.length > 0 && (
                <div className="movie-grid">
                    {movies.map((movie) => (
                    <article
                        className="movie-card clickable-movie-card"
                        key={movie.id}
                        role="button"
                        tabIndex="0"
                        onClick={() => navigate(`/movies/${movie.id}`)}
                        onKeyDown={(event) => {
                            if (event.key === "Enter" || event.key === " ") {
                            event.preventDefault();
                            navigate(`/movies/${movie.id}`);
                            }
                        }}
                    >
                        <div className="poster-container">
                        <img
                            src={
                            movie.posterUrl || "/movie-placeholder.svg"
                            }
                            alt={`${movie.title} poster`}
                            onError={(event) => {
                            event.currentTarget.onerror = null;
                            event.currentTarget.src =
                                "/movie-placeholder.svg";
                            }}
                        />
                        </div>

                        <div className="movie-card-top">
                        <span className="genre-badge">
                            {movie.genre}
                        </span>

                        <span className="movie-year">
                            {movie.releaseYear}
                        </span>
                        </div>

                        <h3>{movie.title}</h3>

                        {movie.description && (
                        <p className="movie-description">
                            {movie.description}
                        </p>
                        )}
                    </article>
                    ))}
                </div>
                )}
            </>
            )}
        </section>
        </main>
    </div>
    );
}

export default MovieListPage;