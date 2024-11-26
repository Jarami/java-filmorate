package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

@Slf4j
@Repository
@Qualifier("db")
@RequiredArgsConstructor
public class DbFilmLikeStorage implements FilmLikeStorage {

    private static final String COUNT_QUERY = """
        SELECT COUNT(*) as "cnt"
        FROM film_likes
        WHERE film_id = :filmId AND user_id = :userId""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_likes (film_id, user_id)
        VALUES (:filmId, :userId)""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_likes
        WHERE film_id = :filmId AND user_id = :userId""";

    protected final NamedParameterJdbcTemplate namedTemplate;

    @Override
    public boolean like(Film film, User user) {

        if (count(film, user) == 0) {
            log.info("liking film {} by {}", film.getName(), user.getLogin());

            namedTemplate.update(INSERT_QUERY,
                    Map.of("filmId", film.getId(), "userId", user.getId()));

            log.info("liking film {} by {} done", film.getName(), user.getLogin());

            return true;

        } else {

            log.info("film {} was already liked by {}", film.getName(), user.getLogin());
            return false;
        }

    }

    @Override
    public boolean dislike(Film film, User user) {

        if (count(film, user) > 0) {

            log.info("disliking film {} by {}", film.getName(), user.getLogin());

            namedTemplate.update(DELETE_QUERY,
                    Map.of("filmId", film.getId(), "userId", user.getId()));

            log.info("disliking film {} by {} done", film.getName(), user.getLogin());

            return true;
        } else {

            log.info("film {} was not liked by {}, skipping", film.getName(), user.getLogin());
            return false;
        }

    }

    private int count(Film film, User user) {
        try {
            Integer result = namedTemplate.queryForObject(COUNT_QUERY,
                    Map.of("filmId", film.getId(), "userId", user.getId()),
                    Integer.class);

            return result == null ? 0 : result;

        } catch (EmptyResultDataAccessException ignored) {
            return 0;
        }
    }
}

