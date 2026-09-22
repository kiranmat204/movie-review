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

  const [showReviewForm, setShowReviewForm] = useState(false);
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewSubmitError, setReviewSubmitError] = useState("");
  const [ratingFilter, setRatingFilter] = useState("all");
  const [deletingReviewId, setDeletingReviewId] = useState(null);
  const [reviewForm, setReviewForm] = useState({
    reviewerName: "",
    rating: "5",
    comment: ""
  });

  const [reviewToDelete, setReviewToDelete] = useState(null);
  const [reviewDeleteError, setReviewDeleteError] = useState("");

  const filteredReviews =
    ratingFilter === "all"
      ? reviews
      : reviews.filter(
          (review) =>
            review.rating === Number(ratingFilter)
        );

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

  // Updates a field in the create review form
  function handleReviewChange(event) {
    const { name, value } = event.target;

    setReviewForm((currentForm) => ({
      ...currentForm,
      [name]: value
    }));
  }

  // Creates a review using the review service
  async function handleCreateReview(event) {
    event.preventDefault();

    try {
      setSubmittingReview(true);
      setReviewSubmitError("");

      const response = await fetch(
        `${REVIEW_API_URL}/${movieId}/reviews`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            reviewerName:
              reviewForm.reviewerName.trim(),
            rating: Number(reviewForm.rating),
            comment: reviewForm.comment.trim()
          })
        }
      );

      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => null);

        throw new Error(
          errorData?.error ||
            errorData?.message ||
            "Unable to create review"
        );
      }

      const createdReview = await response.json();

      setReviews((currentReviews) => [
        createdReview,
        ...currentReviews
      ]);

      setReviewForm({
        reviewerName: "",
        rating: "5",
        comment: ""
      });

      setShowReviewForm(false);
    } catch (exception) {
      setReviewSubmitError(exception.message);
    } finally {
      setSubmittingReview(false);
    }
  }

  // Opens the review form and clears old errors
  function openReviewForm() {
    setReviewSubmitError("");
    setShowReviewForm(true);
  }

  // Cancels creating a review
  function cancelReviewForm() {
    setReviewSubmitError("");
    setShowReviewForm(false);

    setReviewForm({
      reviewerName: "",
      rating: "5",
      comment: ""
    });
  }

  // Opens the delete confirmation for a review
  function openReviewDeleteModal(review) {
    setReviewDeleteError("");
    setReviewToDelete(review);
  }

  // Closes the review delete confirmation
  function closeReviewDeleteModal() {
    if (!deletingReviewId) {
      setReviewDeleteError("");
      setReviewToDelete(null);
    }
  }

  // Deletes the selected review from the review service
  async function handleDeleteReview(event) {
    event.preventDefault();

    if (!reviewToDelete) {
      return;
    }

    try {
      setDeletingReviewId(reviewToDelete.id);
      setReviewDeleteError("");

      const response = await fetch(
        `${REVIEW_API_URL}/${movieId}/reviews/${reviewToDelete.id}`,
        {
          method: "DELETE"
        }
      );

      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => null);

        throw new Error(
          errorData?.error ||
            errorData?.message ||
            "Unable to delete review"
        );
      }

      setReviews((currentReviews) =>
        currentReviews.filter(
          (review) => review.id !== reviewToDelete.id
        )
      );

      setReviewToDelete(null);
    } catch (exception) {
      setReviewDeleteError(exception.message);
    } finally {
      setDeletingReviewId(null);
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

                <div className="review-controls">
                  <label className="rating-filter">
                    <span>Rating</span>

                    <select
                      value={ratingFilter}
                      onChange={(event) =>
                        setRatingFilter(event.target.value)
                      }
                    >
                      <option value="all">All ratings</option>
                      <option value="5">5 stars</option>
                      <option value="4">4 stars</option>
                      <option value="3">3 stars</option>
                      <option value="2">2 stars</option>
                      <option value="1">1 star</option>
                    </select>
                  </label>

                  <button
                    type="button"
                    className="add-review-button"
                    onClick={openReviewForm}
                  >
                    + Add review
                  </button>
                </div>
              </div>

              {showReviewForm && (
                <form
                  className="review-form"
                  onSubmit={handleCreateReview}
                >
                  <div className="review-form-heading">
                    <div>
                      <h3>Add a review</h3>
                      <p>Share your thoughts about this movie.</p>
                    </div>

                    <button
                      type="button"
                      className="close-button"
                      onClick={cancelReviewForm}
                      aria-label="Close review form"
                      disabled={submittingReview}
                    >
                      ×
                    </button>
                  </div>

                  {reviewSubmitError && (
                    <div className="error-message form-error">
                      {reviewSubmitError}
                    </div>
                  )}

                  <div className="review-form-fields">
                    <label>
                      Your name
                      <input
                        type="text"
                        name="reviewerName"
                        value={reviewForm.reviewerName}
                        onChange={handleReviewChange}
                        placeholder="Enter your name"
                        required
                      />
                    </label>

                    <label>
                      Rating
                      <select
                        name="rating"
                        value={reviewForm.rating}
                        onChange={handleReviewChange}
                        required
                      >
                        <option value="5">5 — Excellent</option>
                        <option value="4">4 — Good</option>
                        <option value="3">3 — Average</option>
                        <option value="2">2 — Poor</option>
                        <option value="1">1 — Very poor</option>
                      </select>
                    </label>

                    <label className="review-comment-field">
                      Comment
                      <textarea
                        name="comment"
                        value={reviewForm.comment}
                        onChange={handleReviewChange}
                        placeholder="Write your review..."
                        rows="5"
                        required
                      />
                    </label>
                  </div>

                  <div className="review-form-actions">
                    <button
                      type="button"
                      className="secondary-button"
                      onClick={cancelReviewForm}
                      disabled={submittingReview}
                    >
                      Cancel
                    </button>

                    <button
                      type="submit"
                      className="submit-button"
                      disabled={submittingReview}
                    >
                      {submittingReview
                        ? "Submitting..."
                        : "Submit review"}
                    </button>
                  </div>
                </form>
              )}

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
                    <p>Be the first person to review this movie.</p>
                  </div>
                )}

              {!loadingReviews &&
                !reviewError &&
                reviews.length > 0 &&
                filteredReviews.length === 0 && (
                  <div className="empty-state">
                    <h3>No matching reviews</h3>
                    <p>
                      There are no {ratingFilter}-star reviews for
                      this movie.
                    </p>
                  </div>
                )}

              {!loadingReviews &&
                !reviewError &&
                filteredReviews.length > 0 && (
                  <div className="review-list">
                    {filteredReviews.map((review) => (
                      <article
                        className="review-card"
                        key={review.id}
                      >
                        <div className="review-card-heading">
                          <div className="review-author">
                            <h3>{review.reviewerName}</h3>

                            <span
                              className="review-rating"
                              aria-label={`${review.rating} out of 5 stars`}
                            >
                              {"★".repeat(Number(review.rating))}

                              <span className="empty-stars">
                                {"☆".repeat(5 - Number(review.rating))}
                              </span>
                            </span>
                          </div>

                          <button
                            type="button"
                            className="review-delete-button"
                            onClick={() => openReviewDeleteModal(review)}
                            disabled={deletingReviewId === review.id}
                            title="Delete review"
                            aria-label={`Delete review by ${review.reviewerName}`}
                          >
                            🗑
                          </button>
                        </div>

                        <p>{review.comment}</p>
                        <hr className="review-divider" />
                      </article>
                    ))}
                  </div>
                )}
            </section>
          </>
        )}
      </main>
      
      {/* Delete Modal to show user before confirming movie deletion */}
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
      {/* Review delete modal */}
      {reviewToDelete && (
        <div
          className="modal-overlay"
          onMouseDown={closeReviewDeleteModal}
        >
          <form
            className="delete-confirmation"
            onSubmit={handleDeleteReview}
            onMouseDown={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-review-title"
          >
            <div className="delete-warning-icon">!</div>

            <h2 id="delete-review-title">
              Delete review?
            </h2>

            <p>
              Are you sure you want to delete the review by{" "}
              <strong>
                {reviewToDelete.reviewerName}
              </strong>
              ?
            </p>

            <p className="delete-warning-text">
              This action cannot be undone.
            </p>

            {reviewDeleteError && (
              <div className="error-message form-error">
                {reviewDeleteError}
              </div>
            )}

            <div className="delete-confirmation-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={closeReviewDeleteModal}
                disabled={deletingReviewId !== null}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="confirm-delete-button"
                disabled={deletingReviewId !== null}
              >
                {deletingReviewId !== null
                  ? "Deleting..."
                  : "Delete review"}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}

export default MovieDetailsPage;