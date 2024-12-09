DELETE FROM film_genres;
INSERT INTO film_genres (genre_name)
VALUES ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');

DELETE FROM film_mpa;
INSERT INTO film_mpa (mpa_name)
VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');
