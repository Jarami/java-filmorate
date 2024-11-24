package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class DbFilmStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
            SELECT f.film_id as "film_id",
                   f.film_name as "film_name",
                   f.description as "description",
                   f.release_date as "release_date",
                   f.duration as "duration",
                   fr.rating_id as "rating_id",
                   fr.rating_name as "rating_name",
                   count(fl.film_id) as "rate"
            FROM films f
            INNER JOIN film_ratings fr ON f.rating_id = fr.rating_id
            LEFT JOIN film_likes fl ON fl.film_id = f.film_id
            GROUP BY f.film_id, fr.rating_id""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT f.film_id as "film_id",
               f.film_name as "film_name",
               f.description as "description",
               f.release_date as "release_date",
               f.duration as "duration",
               fr.rating_id as "rating_id",
               fr.rating_name as "rating_name",
               count(fl.film_id) as "rate"
        FROM films f
        INNER JOIN film_ratings fr ON f.rating_id = fr.rating_id
        LEFT JOIN film_likes fl ON fl.film_id = f.film_id
        WHERE f.film_id = ?
        GROUP BY f.film_id, fr.rating_id""";

    private static final String FIND_FILM_GENRES_QUERY = """
        SELECT g.genre_id as "id",
               g.genre_name as "name"
        FROM film_genres g
        INNER JOIN films_genres_relation r ON g.genre_id = r.genre_id
        WHERE r.film_id = ?""";

    private static final String INSERT_QUERY = """
        INSERT INTO films(film_name, description, release_date, duration, rating_id)
        VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
        UPDATE films
        SET film_name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
        WHERE film_id = ?""";

    private static final String DELETE_QUERY = """
        DELETE FROM films
        WHERE film_id = ?""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM films""";

    private static final String INSERT_FILM_GENRE_REL_QUERY = """
        INSERT INTO films_genres_relation (film_id, genre_id)
        VALUES (?, ?)""";

    private static final String DELETE_FILM_GENRE_REL_QUERY = """
        DELETE FROM films_genres_relation
        WHERE film_id = ?""";

    public DbFilmStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);

        films.forEach(film -> {
            List<FilmGenre> genres = jdbc.query(FIND_FILM_GENRES_QUERY,
                    new BeanPropertyRowMapper<FilmGenre>(FilmGenre.class), film.getId());
            film.setGenres(genres);
        });

        return films;
    }

    public Optional<Film> getById(Long FilmId) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, FilmId);

        film.ifPresent(f -> {
            List<FilmGenre> genres = jdbc.query(FIND_FILM_GENRES_QUERY,
                    new BeanPropertyRowMapper<FilmGenre>(FilmGenre.class), f.getId());
            f.setGenres(genres);
        });

        return film;
    }

    public Film save(Film film) {

        if (film.getId() == null) {
            Number id = insert(
                    INSERT_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getRating().getId());

            film.setId((long)id);

        } else {
            update(
                    UPDATE_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getRating().getId(),
                    film.getId()
            );
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveGenres(film);
        }

        return film;
    }

    private void saveGenres(Film film) {

        delete(DELETE_FILM_GENRE_REL_QUERY, film.getId());

        List<FilmGenre> genres = film.getGenres();

        jdbc.batchUpdate(INSERT_FILM_GENRE_REL_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FilmGenre genre = genres.get(i);
                ps.setLong(1, film.getId());
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
    }

    public void delete(Film film) {
        delete(DELETE_QUERY, film.getId());
    }

    public int deleteAll() {
        return executeUpdate(DELETE_ALL_QUERY);
    }
}
