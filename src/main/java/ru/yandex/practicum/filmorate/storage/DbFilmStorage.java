package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("db")
public class DbFilmStorage extends NamedRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.mpa_id as "mpa_id",
                   fr.mpa_name as "mpa_name",
                   count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            GROUP BY f.film_id, fr.mpa_id""";

    private static final String FIND_TOP_QUERY = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.mpa_id as "mpa_id",
                   fr.mpa_name as "mpa_name",
                   count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            LIMIT :count""";

    private static final String FIND_RECOMMENDATIONS_QUERY = """
            SELECT f.film_id AS "film_id",
                   f.film_name AS "film_name",
                   f.description AS "description",
                   f.release_date AS "release_date",
                   f.duration AS "duration",
                   fr.mpa_id AS "mpa_id",
                   fr.mpa_name AS "mpa_name",
                   count(fl.film_id) AS "rate"
            FROM films f
            INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.film_id IN (
                SELECT film_id
                FROM film_likes fl
                WHERE user_id IN (
                    SELECT fl.user_id
                    FROM film_likes fl
                    INNER JOIN film_likes fl2 ON fl.film_id = fl2.film_id
                    WHERE fl.user_id <> :userId AND fl2.user_id = :userId
                    GROUP BY fl.user_id
                    ORDER BY count(fl.film_id) DESC
                    LIMIT 1
                )

                EXCEPT

                SELECT film_id
                FROM film_likes fl
                WHERE user_id = :userId
            )
            GROUP BY f.film_id, fr.mpa_id
            """;

    private static final String FIND_COMMON_FILMS = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.mpa_id as "mpa_id",
                   fr.mpa_name as "mpa_name",
                   count(fl.film_id) as "rate"
            FROM films f
            LEFT JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            WHERE f.film_id IN (
                SELECT fl1.film_id
                FROM film_likes fl1
                INNER JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
                WHERE fl1.user_id = :userId AND fl2.user_id = :friendId
            )
            GROUP BY f.film_id, fr.mpa_id
            ORDER BY count(fl.film_id) desc
            """;

    private static final String FIND_BY_ID_QUERY = """
        SELECT f.film_id as "film_id",
               f.film_name as "film_name",
               f.description as "description",
               f.release_date as "release_date",
               f.duration as "duration",
               fr.mpa_id as "mpa_id",
               fr.mpa_name as "mpa_name",
               count(fl.film_id) as "rate"
        FROM films f
        INNER JOIN film_mpa fr ON f.mpa_id = fr.mpa_id
        LEFT JOIN film_likes fl ON fl.film_id = f.film_id
        WHERE f.film_id = :filmId
        GROUP BY f.film_id, fr.mpa_id""";

    private static final String FIND_FILM_GENRES_QUERY = """
        SELECT g.genre_id as "id",
               g.genre_name as "name"
        FROM film_genres g
        INNER JOIN films_genres_relation r ON g.genre_id = r.genre_id
        WHERE r.film_id = :filmId""";

    private static final String INSERT_QUERY = """
        INSERT INTO films(film_name, description, release_date, duration, mpa_id)
        VALUES (:name, :description, :releaseDate, :duration, :mpaId)""";

    private static final String UPDATE_QUERY = """
        UPDATE films
        SET film_name = :name,
            description = :description,
            release_date = :releaseDate,
            duration = :duration,
            mpa_id = :mpaId
        WHERE film_id = :filmId""";

    private static final String DELETE_QUERY = """
        DELETE FROM films
        WHERE film_id = :filmId""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM films""";

    private static final String INSERT_FILM_GENRE_REL_QUERY = """
        INSERT INTO films_genres_relation (film_id, genre_id)
        VALUES (:filmId, :genreId)""";

    private static final String DELETE_FILM_GENRE_REL_QUERY = """
        DELETE FROM films_genres_relation
        WHERE film_id = :filmId""";

    public DbFilmStorage(NamedParameterJdbcTemplate namedTemplate, FilmRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = getAll(FIND_ALL_QUERY);

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = findMany(FIND_TOP_QUERY, Map.of("count", count));

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }

    @Override
    public List<Film> getRecommendations(long userId) {
        List<Film> films = findMany(FIND_RECOMMENDATIONS_QUERY, Map.of("userId", userId));

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = findMany(FIND_COMMON_FILMS, Map.of("userId", userId, "friendId", friendId));

        films.forEach(film -> {
            List<FilmGenre> genres = findMany(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", film.getId()), new BeanPropertyRowMapper<>(FilmGenre.class));
            film.setGenres(genres);
        });

        return films;
    }

    @Override
    public Optional<Film> getById(Long filmId) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, Map.of("filmId", filmId));

        film.ifPresent(f -> {
            List<FilmGenre> genres = namedTemplate.query(FIND_FILM_GENRES_QUERY,
                    Map.of("filmId", filmId), new BeanPropertyRowMapper<>(FilmGenre.class));
            f.setGenres(genres);
        });

        return film;
    }

    @Override
    public Film save(Film film) {

        if (film.getId() == null) {

            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of(
                        "name", film.getName(),
                        "description", film.getDescription(),
                        "releaseDate", film.getReleaseDate(),
                        "duration", film.getDuration(),
                        "mpaId", film.getMpa().getId()),
                    new String[]{"film_id"}
            );
            Long id = keyHolder.getKeyAs(Long.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать фильм " + film);
            } else {
                film.setId(id);
                log.debug("Фильм {} сохранен с id = {}", film.getName(), film.getId());
            }

        } else {

            update(
                    UPDATE_QUERY,
                    Map.of(
                        "name", film.getName(),
                        "description", film.getDescription(),
                        "releaseDate", film.getReleaseDate(),
                        "duration", film.getDuration(),
                        "mpaId", film.getMpa().getId(),
                        "filmId", film.getId())
            );
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film);
        }

        return film;
    }

    @Override
    public void delete(Film film) {
        delete(DELETE_QUERY, Map.of("filmId", film.getId()));
    }

    @Override
    public int deleteAll() {
        return delete(DELETE_ALL_QUERY);
    }

    private void saveGenres(Film film) {

        delete(DELETE_FILM_GENRE_REL_QUERY, Map.of("filmId", film.getId()));

        List<Map<String, Object>> batchValues = film.getGenres().stream()
                .map(genre -> createFilmGenreMap(film, genre))
                .toList();

        batchUpdate(INSERT_FILM_GENRE_REL_QUERY, batchValues);
    }

    private Map<String, Object> createFilmGenreMap(Film film, FilmGenre genre) {
        return Map.of("filmId", film.getId(), "genreId", genre.getId());
    }
}
