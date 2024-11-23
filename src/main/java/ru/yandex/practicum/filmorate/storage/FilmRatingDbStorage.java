package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRatingMapper;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("db")
public class FilmRatingDbStorage extends BaseRepository<FilmRating> implements FilmRatingStorage {

    private static final String FIND_ALL_QUERY = """
        SELECT rating_id as "rating_id",
               rating_name as "rating_name"
        FROM film_ratings""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT rating_id as "rating_id",
               rating_name as "rating_name"
        FROM film_ratings
        WHERE rating_id = ?""";

    private static final String FIND_BY_NAME_QUERY = """
        SELECT rating_id as "rating_id",
               rating_name as "rating_name"
        FROM film_ratings
        WHERE rating_name = ?""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_ratings(rating_name)
        VALUES (?, ?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
        UPDATE film_ratings
        SET rating_name = ?
        WHERE rating_id = ?""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_ratings
        WHERE rating_id = ?""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM film_ratings""";

    public FilmRatingDbStorage(JdbcTemplate jdbc, FilmRatingMapper mapper) {
        super(jdbc, mapper);
    }

    public List<FilmRating> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<FilmRating> getById(Integer filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    public Optional<FilmRating> getByName(String filmRatingName) {
        return findOne(FIND_BY_NAME_QUERY, filmRatingName);
    }

    public FilmRating save(FilmRating rating) {

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

    public void delete(FilmRating rating) {
        delete(DELETE_QUERY, rating.getId());
    }

    public int deleteAll() {
        return executeUpdate(DELETE_ALL_QUERY);
    }

}
