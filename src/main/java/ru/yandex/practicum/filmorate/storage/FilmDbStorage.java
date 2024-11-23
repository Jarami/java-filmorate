package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT f.film_id as "film_id",
               f.film_name as "film_name",
               f.description as "description",
               f.release_date as "release_date",
               f.duration as "duration",
               fr.rating_id as "rating_id",
               fr.rating_name as "rating_name"
        FROM films f
        INNER JOIN film_ratings fr ON f.rating_id = fr.rating_id""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT f.film_id as "film_id",
               f.film_name as "film_name",
               f.description as "description",
               f.release_date as "release_date",
               f.duration as "duration",
               fr.rating_id as "rating_id",
               fr.rating_name as "rating_name"
        FROM films f
        INNER JOIN film_ratings fr ON f.rating_id = fr.rating_id
        WHERE film_id = ?""";

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

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Film> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Film> getById(Long FilmId) {
        return findOne(FIND_BY_ID_QUERY, FilmId);
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

        return film;
    }

    public void delete(Film film) {
        delete(DELETE_QUERY, film.getId());
    }

    public int deleteAll() {
        return executeUpdate(DELETE_ALL_QUERY);
    }
}
