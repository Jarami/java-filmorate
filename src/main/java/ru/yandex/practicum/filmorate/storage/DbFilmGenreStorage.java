package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmGenreRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Qualifier("db")
public class DbFilmGenreStorage extends NamedRepository<FilmGenre> implements FilmGenreStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT genre_id as "genre_id",
               genre_name as "genre_name"
        FROM film_genres""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT genre_id as "genre_id",
               genre_name as "genre_name"
        FROM film_genres
        WHERE genre_id = :id""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_genres (genre_name)
        VALUES (:name)""";

    private static final String UPDATE_QUERY = """
        UPDATE film_genres
        SET genre_name = :name
        WHERE genre_id = :id""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_genres
        WHERE genre_id = :id""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM film_genres""";

    public DbFilmGenreStorage(NamedParameterJdbcTemplate namedTemplate, FilmGenreRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public List<FilmGenre> getAll() {
        return super.getAll(FIND_ALL_QUERY);
    }

    @Override
    public Optional<FilmGenre> getById(Integer genreId) {
        return findOne(FIND_BY_ID_QUERY, Map.of("id", genreId));
    }

    @Override
    public List<FilmGenre> getById(List<Integer> ids) {
        return getAll().stream()
                .filter(genre -> ids.contains(genre.getId()))
                .toList();
    }

    @Override
    public FilmGenre save(FilmGenre genre) {

        if (genre.getId() == null) {
            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of("name", genre.getName()),
                    new String[]{"genre_id"}
            );

            Integer id = keyHolder.getKeyAs(Integer.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать жанр " + genre);
            } else {
                genre.setId(id);
            }

        } else {
            update(
                    UPDATE_QUERY,
                    Map.of("id", genre.getId(), "name", genre.getName())
            );
        }

        return genre;
    }

    @Override
    public void delete(FilmGenre genre) {
        delete(DELETE_QUERY, Map.of("id", genre.getId()));
    }

    @Override
    public int deleteAll() {
        return delete(DELETE_ALL_QUERY);
    }

}

