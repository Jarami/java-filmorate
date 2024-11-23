DELETE FROM film_genres;
INSERT INTO film_genres (genre_name)
VALUES ('comedy'), ('drama'), ('cartoon'), ('thriller'), ('documentary'), ('action');

DELETE FROM film_ratings;
INSERT INTO film_ratings (rating_name)
VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');