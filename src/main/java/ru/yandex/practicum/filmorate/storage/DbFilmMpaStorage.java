package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMpaRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Qualifier("db")
public class DbFilmMpaStorage extends NamedRepository<FilmMpa> implements FilmMpaStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT mpa_id as "mpa_id",
               mpa_name as "mpa_name"
        FROM film_mpa""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT mpa_id as "mpa_id",
               mpa_name as "mpa_name"
        FROM film_mpa
        WHERE mpa_id = :id""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_mpa(mpa_name)
        VALUES (:name)""";

    private static final String UPDATE_QUERY = """
        UPDATE film_mpa
        SET mpa_name = :name
        WHERE mpa_id = :id""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_mpa
        WHERE mpa_id = :id""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM film_mpa""";

    public DbFilmMpaStorage(NamedParameterJdbcTemplate namedTemplate, FilmMpaRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public List<FilmMpa> getAll() {
        return super.getAll(FIND_ALL_QUERY);
    }

    @Override
    public Optional<FilmMpa> getById(Integer filmId) {
        return findOne(FIND_BY_ID_QUERY, Map.of("id", filmId));
    }

    @Override
    public FilmMpa save(FilmMpa mpa) {

        if (mpa.getId() == null) {
            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of("name", mpa.getName()),
                    new String[]{"mpa_id"}
            );

            Integer id = keyHolder.getKeyAs(Integer.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать рейтинг " + mpa);
            } else {
                mpa.setId(id);
            }

        } else {
            update(
                    UPDATE_QUERY,
                    Map.of("id", mpa.getId(), "name", mpa.getName())
            );
        }

        return mpa;
    }

    @Override
    public void delete(FilmMpa mpa) {
        delete(DELETE_QUERY, Map.of("id", mpa.getId()));
    }

    @Override
    public int deleteAll() {
        return delete(DELETE_ALL_QUERY);
    }
}
