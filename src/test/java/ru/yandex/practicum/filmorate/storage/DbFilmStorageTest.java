package ru.yandex.practicum.filmorate.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import ru.yandex.practicum.filmorate.storage.mapper.*;
import ru.yandex.practicum.filmorate.util.TestUtil;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ DbFilmLikeStorage.class,
        DbFilmGenreStorage.class, FilmGenreRowMapper.class,
        DbFilmStorage.class, FilmRowMapper.class,
        DbUserStorage.class, UserRowMapper.class,
        DbFilmMpaStorage.class, FilmMpaRowMapper.class,
        DirectorRowMapper.class})
public class DbFilmStorageTest {

    private final DbFilmGenreStorage filmGenreStorage;
    private final DbFilmLikeStorage filmLikeStorage;
    private final DbFilmStorage filmStorage;
    private final DbUserStorage userStorage;
    private final DbFilmMpaStorage filmMpaStorage;

    private List<FilmGenre> allGenres;
    private List<FilmMpa> allMpa;

    private Film film1;
    private Film film2;
    private Film film3;
    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        shutdown();

        allMpa = createMpa("G", "PG", "PG-13", "R", "NC-17");
        allGenres = createGenres("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный");

        film1 = createFilm("G", List.of("Комедия", "Драма"));
        film2 = createFilm("PG", List.of("Документальный", "Драма"));
        film3 = createFilm("R", List.of("Мультфильм", "Комедия"));

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
    void givenFilm_whenDelete_gotDeleted() {
        filmStorage.delete(film1);
        List<Film> actFilms = filmStorage.getAll();

        Set<String> actFilmNames = Set.of(film2.getName(), film3.getName());

        assertEquals(2, actFilms.size());
        assertEquals(actFilmNames, getFilmNames(actFilms));
    }

    @Test
    void givenFilms_whenGetAll_gotIt() {
        List<Film> actFilms = filmStorage.getAll();
        assertEquals(3, actFilms.size());
    }

    @Test
    void givenFilms_whenGetById_gotIt() {
        Film actFilm = filmStorage.getById(film1.getId()).get();

        assertEquals(actFilm.getName(), film1.getName());
    }

    @Test
    void givenTwoLikedFilms_whenGotPopular1_gotMostPopular() {
        filmLikeStorage.like(film2, user1);
        filmLikeStorage.like(film2, user2);
        filmLikeStorage.like(film1, user1);

        List<Film> films = filmStorage.getPopularFilms(1);
        assertEquals(1, films.size());
        assertEquals(film2.getId(), films.get(0).getId());
    }

    @Test
    void givenTwoLikedFilms_whenGotPopular10_gotAllFilms() {
        filmLikeStorage.like(film2, user1);
        filmLikeStorage.like(film2, user2);
        filmLikeStorage.like(film1, user1);

        List<Film> films = filmStorage.getPopularFilms(10);
        assertEquals(3, films.size());
        assertEquals(film2.getId(), films.get(0).getId());
        assertEquals(film1.getId(), films.get(1).getId());
        assertEquals(film3.getId(), films.get(2).getId());
    }

    @Test
    void givenFilm_whenUpdate_gotUpdated() {
        film1.setName("name");
        film1.setDescription("desc");
        film1.setReleaseDate(LocalDate.parse("1985-10-26"));
        film1.setDuration(10);
        film1.setMpa(mpaByName("NC-17"));
        film1.setGenres(genresByNames("Мультфильм", "Триллер"));

        filmStorage.save(film1);

        Film actFilm = filmStorage.getById(film1.getId()).get();

        assertEquals("name", actFilm.getName());
        assertEquals("desc", actFilm.getDescription());
        assertEquals("1985-10-26", actFilm.getReleaseDate().toString());
        assertEquals(10, actFilm.getDuration());
        assertEquals("NC-17", actFilm.getMpa().getName());
        assertEquals(Set.of("Мультфильм", "Триллер"), getGenresNames(actFilm));
    }

    @Test
    void givenUserWithRecommendations_whenGetRecommendations_gotCorrectFilm() {
        filmLikeStorage.like(film1, user1);
        filmLikeStorage.like(film2, user1);
        filmLikeStorage.like(film2, user2);
        filmLikeStorage.like(film3, user2);

        List<Film> recommendedFilms = filmStorage.getRecommendations(user1.getId());

        assertEquals(1, recommendedFilms.size());
        assertEquals(film3.getId(), recommendedFilms.get(0).getId());
    }

    @Test
    void givenUserWithoutRecommendations_whenGetRecommendations_gotEmptyList() {
        filmLikeStorage.like(film1, user1);
        filmLikeStorage.like(film2, user2);

        List<Film> recommendedFilms = filmStorage.getRecommendations(user1.getId());

        assertEquals(0, recommendedFilms.size());
    }

    @Test
    void givenNoLikes_whenGetRecommendations_gotEmptyList() {
        List<Film> recommendedFilms = filmStorage.getRecommendations(user1.getId());

        assertEquals(0, recommendedFilms.size());
    }

    private Set<String> getFilmNames(List<Film> films) {
        return films.stream().map(f -> f.getName()).collect(Collectors.toSet());
    }

    private FilmGenre genreByName(String name) {
        return allGenres.stream().filter(g -> g.getName().equals(name)).findAny().get();
    }

    private List<FilmGenre> genresByNames(String... names) {
        return Arrays.stream(names).map(name -> genreByName(name)).toList();
    }

    private Set<String> getGenresNames(Film film) {
        return film.getGenres().stream().map(FilmGenre::getName).collect(Collectors.toSet());
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
