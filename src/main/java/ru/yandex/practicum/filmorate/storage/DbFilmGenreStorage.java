package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmGenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class DbFilmGenreStorage extends BaseRepository<FilmGenre> implements FilmGenreStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT genre_id as "genre_id",
               genre_name as "genre_name"
        FROM film_genres""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT genre_id as "genre_id",
               genre_name as "genre_name"
        FROM film_genres
        WHERE genre_id = ?""";

    private static final String FIND_BY_NAME_QUERY = """
        SELECT genre_id as "genre_id",
               genre_name as "genre_name"
        FROM film_genres
        WHERE genre_name = ?""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_genres(genre_name)
        VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
        UPDATE film_genres
        SET genre_name = ?
        WHERE genre_id = ?""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_genres
        WHERE genre_id = ?""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM film_genres""";

    public DbFilmGenreStorage(JdbcTemplate jdbc, FilmGenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<FilmGenre> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<FilmGenre> getById(Integer genreId) {
        return findOne(FIND_BY_ID_QUERY, genreId);
    }
    
    public List<FilmGenre> getById(List<Integer> ids) {
        return getAll().stream().filter(genre -> ids.contains(genre.getId())).toList();
    }

    public Optional<FilmGenre> getByName(String filmRatingName) {
        return findOne(FIND_BY_NAME_QUERY, filmRatingName);
    }

    public FilmGenre save(FilmGenre rating) {

        if (rating.getId() == null) {
            Number id = insert(
                    INSERT_QUERY,
                    rating.getName()
            );
            rating.setId((int)id);

        } else {
            update(
                    UPDATE_QUERY,
                    rating.getName(),
                    rating.getId()
            );
        }

        return rating;
    }

    public void delete(FilmGenre rating) {
        delete(DELETE_QUERY, rating.getId());
    }

    public int deleteAll() {
        return executeUpdate(DELETE_ALL_QUERY);
    }

}

