package com.example.movie_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Movie title is required")
    @Size(max = 200, message = "Movie title cannot exceed 200 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Min(value = 1888, message = "Release year must be 1888 or later")
    @Max(value = 2100, message = "Release year cannot be later than 2100")
    @Column(name = "release_year", nullable = false)
    private int releaseYear;

    @NotBlank(message = "Genre is required")
    @Size(max = 100, message = "Genre cannot exceed 100 characters")
    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name = "poster_url")
    private String posterUrl;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    public Movie(){

    }

    public Movie(String title, int releaseYear, String genre){
        this.title = title;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    public Movie(String title, int releaseYear, String genre, String posterUrl) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.posterUrl = posterUrl;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
