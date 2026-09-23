import { useState } from "react";

const MOVIE_API_URL = "http://localhost:8081/api/movies";

function CreateMovieForm({ onCreated, onCancel }) {
  const [formData, setFormData] = useState({
    title: "",
    releaseYear: "",
    genre: "",
    posterUrl: "",
    description: ""
  });

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  // Updates the form value that the user changes
  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((currentData) => ({
      ...currentData,
      [name]: value
    }));
  }

  // Sends the new movie to the movie service
  async function handleSubmit(event) {
    event.preventDefault();

    try {
      setSubmitting(true);
      setError("");

      const response = await fetch(MOVIE_API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          title: formData.title.trim(),
          releaseYear: Number(formData.releaseYear),
          genre: formData.genre.trim(),
          posterUrl: formData.posterUrl.trim() || null,
          description: formData.description.trim() || null
        })
      });

      // Handles an HTTP error returned by movie-service
      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => null);

        if (response.status >= 500) {
          throw new Error(
            "Movie service is currently unavailable. Please try again later."
          );
        }

        // Handles duplicate movies and validation errors
        throw new Error(
          errorData?.error ||
            errorData?.message ||
            "Unable to create movie"
        );
      }

      const createdMovie = await response.json();

      await onCreated(createdMovie);
    } catch (exception) {
      if (exception instanceof TypeError) {
        setError(
          "Movie service is currently unavailable. Please try again later."
        );
      } else {
        setError(exception.message);
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="movie-form-panel">
      <form className="movie-form" onSubmit={handleSubmit}>
        {error && (
          <div className="error-message form-error">
            {error}
          </div>
        )}

        <label>
          Movie title
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder="For example, Interstellar"
            required
          />
        </label>

        <label>
          Release year
          <input
            type="number"
            name="releaseYear"
            value={formData.releaseYear}
            onChange={handleChange}
            placeholder="2014"
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
            placeholder="Science Fiction"
            required
          />
        </label>

        <label className="poster-field">
          Poster URL
          <input
            type="url"
            name="posterUrl"
            value={formData.posterUrl}
            onChange={handleChange}
            placeholder="https://example.com/movie-poster.jpg"
          />
        </label>

        <label className="description-field">
          Description
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="Write a short description of the movie..."
            rows="4"
            maxLength="1000"
          />
        </label>

        <div className="form-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={onCancel}
            disabled={submitting}
          >
            Cancel
          </button>

          <button
            type="submit"
            className="submit-button"
            disabled={submitting}
          >
            {submitting ? "Creating..." : "Create movie"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateMovieForm;