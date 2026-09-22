import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../App.css";

const MOVIE_API_URL = "http://localhost:8081/api/movies";
const REVIEW_API_URL = "http://localhost:8082/api/movies";

function MovieDetailsPage() {
  const { movieId } = useParams();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  const [formData, setFormData] = useState({
    title: "",
    releaseYear: "",
    genre: "",
    posterUrl: "",
    description: ""
  });

  const [loadingMovie, setLoadingMovie] = useState(true);
  const [loadingReviews, setLoadingReviews] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);

  const [movieError, setMovieError] = useState("");
  const [reviewError, setReviewError] = useState("");
  const [saveError, setSaveError] = useState("");

  // Loads the selected movie from the movie service
  async function loadMovie() {
    try {
      setLoadingMovie(true);
      setMovieError("");

      const response = await fetch(
        `${MOVIE_API_URL}/${movieId}`
      );

      if (!response.ok) {
        throw new Error("Unable to load this movie");
      }

      const data = await response.json();

      setMovie(data);
      setFormData({
        title: data.title || "",
        releaseYear: data.releaseYear || "",
        genre: data.genre || "",
        posterUrl: data.posterUrl || "",
        description: data.description || ""
      });
    } catch (exception) {
      setMovieError(exception.message);
    } finally {
      setLoadingMovie(false);
    }
  }

  // Loads all reviews belonging to the selected movie
  async function loadReviews() {
    try {
      setLoadingReviews(true);
      setReviewError("");

      const response = await fetch(
        `${REVIEW_API_URL}/${movieId}/reviews`
      );

      if (!response.ok) {
        throw new Error("Unable to load reviews");
      }

      const data = await response.json();
      setReviews(data);
    } catch (exception) {
      setReviewError(exception.message);
    } finally {
      setLoadingReviews(false);
    }
  }

  // Updates the form field changed by the user
  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value
    }));
  }

  // Enables editing and resets the form to the saved movie
  function startEditing() {
    setFormData({
      title: movie.title || "",
      releaseYear: movie.releaseYear || "",
      genre: movie.genre || "",
      posterUrl: movie.posterUrl || "",
      description: movie.description || ""
    });

    setSaveError("");
    setEditing(true);
  }

  // Cancels editing without saving changes
  function cancelEditing() {
    setSaveError("");
    setEditing(false);
  }

  // Saves all updated movie fields
  async function handleSave(event) {
    event.preventDefault();

    try {
      setSaving(true);
      setSaveError("");

      const response = await fetch(
        `${MOVIE_API_URL}/${movieId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            title: formData.title.trim(),
            releaseYear: Number(formData.releaseYear),
            genre: formData.genre.trim(),
            posterUrl: formData.posterUrl.trim() || null,
            description:
              formData.description.trim() || null
          })
        }
      );

      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => null);

        throw new Error(
          errorData?.error || "Unable to update movie"
        );
      }

      const updatedMovie = await response.json();

      setMovie(updatedMovie);
      setFormData({
        title: updatedMovie.title || "",
        releaseYear: updatedMovie.releaseYear || "",
        genre: updatedMovie.genre || "",
        posterUrl: updatedMovie.posterUrl || "",
        description: updatedMovie.description || ""
      });

      setEditing(false);
    } catch (exception) {
      setSaveError(exception.message);
    } finally {
      setSaving(false);
    }
  }

  // Deletes the selected movie from the movie service
  async function handleDelete(event) {
    event.preventDefault();

    try {
      setDeleting(true);
      setDeleteError("");

      const response = await fetch(
        `${MOVIE_API_URL}/${movieId}`,
        {
          method: "DELETE"
        }
      );

      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => null);

        throw new Error(
          errorData?.error || "Unable to delete movie"
        );
      }

      // Returns to the refreshed movie list
      navigate("/", { replace: true });
    } catch (exception) {
      setDeleteError(exception.message);
    } finally {
      setDeleting(false);
    }
  }

  useEffect(() => {
    loadMovie();
    loadReviews();
  }, [movieId]);

  return (
    <div className="app">
      <header className="app-header details-app-header">
        <div>
          <p className="eyebrow">Reviewflix</p>
          <h1>Movie Details</h1>
          <p className="subtitle">
            View movie information and audience reviews.
          </p>
        </div>
      </header>

      <main className="details-main">
        <button
          type="button"
          className="back-button"
          onClick={() => navigate("/")}
        >
          ← Back to movies
        </button>

        {loadingMovie && (
          <p className="status-message">
            Loading movie...
          </p>
        )}

        {!loadingMovie && movieError && (
          <div className="error-message">
            {movieError}
          </div>
        )}

        {!loadingMovie && !movieError && movie && (
          <>
            <section className="movie-details-panel">
              <div className="details-poster">
                <img
                  src={
                    movie.posterUrl ||
                    "/movie-placeholder.svg"
                  }
                  alt={`${movie.title} poster`}
                  onError={(event) => {
                    event.currentTarget.onerror = null;
                    event.currentTarget.src =
                      "/movie-placeholder.svg";
                  }}
                />
              </div>

              <div className="details-content">
                <div className="details-toolbar">
                  <div>
                    <p className="details-label">
                      Movie information
                    </p>

                    {!editing && <h2>{movie.title}</h2>}
                  </div>

                  {!editing && (
                    <div className="details-toolbar-actions">
                      <button
                        type="button"
                        className="edit-icon-button"
                        onClick={startEditing}
                        title="Edit movie"
                        aria-label="Edit movie"
                      >
                        ✎
                      </button>

                      <button
                        type="button"
                        className="delete-icon-button"
                        onClick={() => {
                          setDeleteError("");
                          setShowDeleteConfirm(true);
                        }}
                        title="Delete movie"
                        aria-label="Delete movie"
                      >
                        🗑
                      </button>
                    </div>
                  )}
                </div>

                {editing ? (
                  <form
                    className="details-edit-form"
                    onSubmit={handleSave}
                  >
                    {saveError && (
                      <div className="error-message form-error">
                        {saveError}
                      </div>
                    )}

                    <label>
                      Movie title
                      <input
                        type="text"
                        name="title"
                        value={formData.title}
                        onChange={handleChange}
                        required
                      />
                    </label>

                    <div className="details-form-row">
                      <label>
                        Release year
                        <input
                          type="number"
                          name="releaseYear"
                          value={formData.releaseYear}
                          onChange={handleChange}
                          min="1888"
                          max="2100"
                          required
                        />
                      </label>

                      <label>
                        Genre
                        <input
                          type="text"
                          name="genre"
                          value={formData.genre}
                          onChange={handleChange}
                          required
                        />
                      </label>
                    </div>

                    <label>
                      Poster URL
                      <input
                        type="url"
                        name="posterUrl"
                        value={formData.posterUrl}
                        onChange={handleChange}
                        placeholder="https://example.com/poster.jpg"
                      />
                    </label>

                    <label>
                      Description
                      <textarea
                        name="description"
                        value={formData.description}
                        onChange={handleChange}
                        rows="7"
                        maxLength="1000"
                      />
                    </label>

                    <div className="details-form-actions">
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={cancelEditing}
                        disabled={saving}
                      >
                        Cancel
                      </button>

                      <button
                        type="submit"
                        className="submit-button"
                        disabled={saving}
                      >
                        {saving
                          ? "Saving..."
                          : "Save changes"}
                      </button>
                    </div>
                  </form>
                ) : (
                  <div className="movie-information">
                    <div className="movie-metadata">
                      <span className="genre-badge">
                        {movie.genre}
                      </span>

                      <span className="details-year">
                        {movie.releaseYear}
                      </span>
                    </div>

                    <div className="description-section">
                      <h3>Description</h3>

                      <p>
                        {movie.description ||
                          "No description has been added."}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </section>

            <section className="reviews-section">
              <div className="reviews-heading">
                <div>
                  <h2>Reviews</h2>
                  <p>
                    {reviews.length}{" "}
                    {reviews.length === 1
                      ? "review"
                      : "reviews"}
                  </p>
                </div>
              </div>

              {loadingReviews && (
                <p className="status-message">
                  Loading reviews...
                </p>
              )}

              {!loadingReviews && reviewError && (
                <div className="error-message">
                  {reviewError}
                </div>
              )}

              {!loadingReviews &&
                !reviewError &&
                reviews.length === 0 && (
                  <div className="empty-state">
                    <h3>No reviews yet</h3>
                    <p>
                      This movie has not been reviewed yet.
                    </p>
                  </div>
                )}

              {!loadingReviews &&
                !reviewError &&
                reviews.length > 0 && (
                  <div className="review-list">
                    {reviews.map((review) => (
                      <article
                        className="review-card"
                        key={review.id}
                      >
                        <div className="review-card-heading">
                          <h3>{review.reviewerName}</h3>

                          <span
                            className="review-rating"
                            aria-label={`${review.rating} out of 5 stars`}
                          >
                            {"★".repeat(review.rating)}

                            <span>
                              {"★".repeat(
                                5 - review.rating
                              )}
                            </span>
                          </span>
                        </div>

                        <p>{review.comment}</p>
                      </article>
                    ))}
                  </div>
                )}
            </section>
          </>
        )}
      </main>
      {showDeleteConfirm && (
        <div
          className="modal-overlay"
          onMouseDown={() => {
            if (!deleting) {
              setShowDeleteConfirm(false);
            }
          }}
        >
          <form
            className="delete-confirmation"
            onSubmit={handleDelete}
            onMouseDown={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-dialog-title"
          >
            <div className="delete-warning-icon">!</div>

            <h2 id="delete-dialog-title">
              Delete movie?
            </h2>

            <p>
              Are you sure you want to delete{" "}
              <strong>{movie?.title}</strong>?
            </p>

            <p className="delete-warning-text">
              This action cannot be undone.
            </p>

            {deleteError && (
              <div className="error-message form-error">
                {deleteError}
              </div>
            )}

            <div className="delete-confirmation-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleting}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="confirm-delete-button"
                disabled={deleting}
              >
                {deleting ? "Deleting..." : "Delete movie"}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

export default MovieDetailsPage;