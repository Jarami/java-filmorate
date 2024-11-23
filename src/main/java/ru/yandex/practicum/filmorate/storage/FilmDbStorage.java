package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = """
        INSERT INTO films(title, description, rating, release_date, duration)
        VALUES (?, ?, ?, ?, ?) returning film_id""";
    private static final String UPDATE_QUERY = """
        UPDATE films SET title = ?, description = ?, rating = ?, release_date = ?, duration = ?
        WHERE film_id = ?""";
    private static final String DELETE_QUERY = """
        DELETE FROM films WHERE film_id = ?""";
    private static final String DELETE_ALL_QUERY = "DELETE FROM films";

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
            long id = insert(
                    INSERT_QUERY,
                    film.getTitle(),
                    film.getDescription(),
                    film.getRating(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration()
            );
            film.setId(id);

        } else {
            update(
                    UPDATE_QUERY,
                    film.getTitle(),
                    film.getDescription(),
                    film.getRating(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration()
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
