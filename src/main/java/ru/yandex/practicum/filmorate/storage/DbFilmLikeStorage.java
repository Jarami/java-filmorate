package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;

@Slf4j
@Repository
@Qualifier("db")
@RequiredArgsConstructor
public class DbFilmLikeStorage implements FilmLikeStorage {

    private static final String COUNT_QUERY = """
        SELECT COUNT(*) as "cnt"
        FROM film_likes
        WHERE film_id = ? AND user_id = ?""";

    private static final String INSERT_QUERY = """
        INSERT INTO film_likes (film_id, user_id)
        VALUES (?, ?)""";

    private static final String DELETE_QUERY = """
        DELETE FROM film_likes
        WHERE film_id = ? AND user_id = ?""";

    protected final JdbcTemplate jdbc;

    public boolean like(Film film, User user) {
        log.debug("liking film = {}, {}", film, count(film, user));
        if (count(film, user) == 0) {
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_QUERY);
                ps.setLong(1, film.getId());
                ps.setLong(2, user.getId());
                return ps;
            });
            log.info("liking film1 = {}", film);
            film.setRate(film.getRate() + 1);
            return true;
        }

        return false;
    };

    public boolean dislike(Film film, User user) {

        if (count(film, user) > 0) {
            jdbc.update(DELETE_QUERY, film.getId(), user.getId());
            film.setRate(film.getRate() - 1);
            return true;
        }

        return false;
    };

    private int count(Film film, User user) {
        return jdbc.queryForObject(COUNT_QUERY, Integer.class, film.getId(), user.getId());
    }
}

