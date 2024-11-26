package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class DbFilmMpaStorage extends BaseRepository<FilmMpa> implements FilmMpaStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT mpa_id as "mpa_id",
               mpa_name as "mpa_name"
        FROM film_mpa""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT mpa_id as "mpa_id",
               mpa_name as "mpa_name"
        FROM film_mpa
        WHERE mpa_id = ?""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_mpa(mpa_name)
        VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
        UPDATE film_mpa
        SET mpa_name = ?
        WHERE mpa_id = ?""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_mpa
        WHERE mpa_id = ?""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM film_mpa""";

    public DbFilmMpaStorage(JdbcTemplate jdbc, FilmMpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<FilmMpa> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<FilmMpa> getById(Integer filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    @Override
    public FilmMpa save(FilmMpa mpa) {

        if (mpa.getId() == null) {
            Number id = insert(
                    INSERT_QUERY,
                    mpa.getName()
            );
            mpa.setId((int)id);

        } else {
            update(
                    UPDATE_QUERY,
                    mpa.getName(),
                    mpa.getId()
            );
        }

        return mpa;
    }

    @Override
    public void delete(FilmMpa mpa) {
        delete(DELETE_QUERY, mpa.getId());
    }

    @Override
    public int deleteAll() {
        return executeUpdate(DELETE_ALL_QUERY);
    }
}
