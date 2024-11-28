package ru.yandex.practicum.filmorate.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FilmGenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.util.TestUtil;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ DbFilmLikeStorage.class,
          DbFilmGenreStorage.class, FilmGenreRowMapper.class,
          DbFilmStorage.class, FilmRowMapper.class,
          DbUserStorage.class, UserRowMapper.class,
          DbFilmMpaStorage.class, FilmMpaRowMapper.class})
public class DbFilmLikeStorageTest {

    private final DbFilmGenreStorage filmGenreStorage;
    private final DbFilmLikeStorage filmLikeStorage;
    private final DbFilmStorage filmStorage;
    private final DbUserStorage userStorage;
    private final DbFilmMpaStorage filmMpaStorage;

    private List<FilmGenre> allGenres;
    private List<FilmMpa> allMpa;

    private Film film1;
    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        shutdown();

        allMpa = createMpa("G", "PG", "PG-13", "R", "NC-17");
        allGenres = createGenres("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный");

        film1 = createFilm("G", List.of("Комедия", "Драма"));

        user1 = createUser();
        user2 = createUser();
    }

    @AfterEach
    void shutdown() {
        filmLikeStorage.deleteAll();
        filmGenreStorage.deleteAll();
        filmMpaStorage.deleteAll();
        filmStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    void givenUnlikedFilm_whenLike_gotLiked() {
        filmLikeStorage.like(film1, user1);
        boolean result = filmLikeStorage.like(film1, user2);

        assertTrue(result);

        Film film = filmStorage.getById(film1.getId()).get();
        assertEquals(2, film.getRate());
    }

    @Test
    void givenUnlikedFilm_whenDislike_gotNothing() {
        filmLikeStorage.like(film1, user1);
        boolean result = filmLikeStorage.dislike(film1, user2);

        assertFalse(result);

        Film film = filmStorage.getById(film1.getId()).get();
        assertEquals(1, film.getRate());
    }

    @Test
    void givenLikedFilm_whenLike_gotNothing() {
        filmLikeStorage.like(film1, user1);
        boolean result = filmLikeStorage.like(film1, user1);

        assertFalse(result);

        Film film = filmStorage.getById(film1.getId()).get();
        assertEquals(1, film.getRate());
    }

    @Test
    void givenLikedFilm_whenDislike_gotDisliked() {
        filmLikeStorage.like(film1, user1);
        boolean result = filmLikeStorage.dislike(film1, user1);

        assertTrue(result);

        Film film = filmStorage.getById(film1.getId()).get();
        assertEquals(0, film.getRate());
    }

    private FilmGenre genreByName(String name) {
        return allGenres.stream().filter(g -> g.getName().equals(name)).findAny().get();
    }

    private FilmMpa mpaByName(String name) {
        return allMpa.stream().filter(g -> g.getName().equals(name)).findAny().get();
    }

    private List<FilmGenre> createGenres(String... genreNames) {
        return Arrays.stream(genreNames)
                .map(genreName -> filmGenreStorage.save(new FilmGenre(null, genreName)))
                .toList();
    }

    private List<FilmMpa> createMpa(String... mpaNames) {
        return Arrays.stream(mpaNames)
                .map(mpaName -> filmMpaStorage.save(new FilmMpa(null, mpaName)))
                .toList();
    }

    private Film createFilm(String mpaName, List<String> genreNames) {

        Film film = TestUtil.getRandomFilm();
        film.setGenres(genreNames.stream().map(genreName -> genreByName(genreName)).toList());
        film.setMpa(mpaByName(mpaName));

        return filmStorage.save(film);
    }

    private User createUser() {
        return userStorage.save(TestUtil.getRandomUser());
    }
}
