CREATE UNIQUE INDEX IF NOT EXISTS uk_movie_title_year
ON movies (title COLLATE NOCASE, release_year);