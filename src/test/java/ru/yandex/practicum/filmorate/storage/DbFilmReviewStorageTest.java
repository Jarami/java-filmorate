package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.mapper.*;
import ru.yandex.practicum.filmorate.util.TestUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.*;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ DbFilmGenreStorage.class, FilmGenreRowMapper.class,
        DbFilmStorage.class, FilmRowMapper.class,
        DbUserStorage.class, UserRowMapper.class,
        DbFilmReviewStorage.class, FilmReviewRowMapper.class,
        DbFilmMpaStorage.class, FilmMpaRowMapper.class,
        DirectorRowMapper.class})
public class DbFilmReviewStorageTest {

    private final DbFilmReviewStorage reviewStorage;
    private final DbUserStorage userStorage;
    private final DbFilmStorage filmStorage;
    private final DbFilmGenreStorage filmGenreStorage;
    private final DbFilmMpaStorage filmMpaStorage;

    private List<FilmGenre> allGenres;
    private List<FilmMpa> allMpa;
    private Random rnd;

    @BeforeEach
    void setup() {
        shutdown();
        rnd = new Random();
        allMpa = createMpa("G", "PG", "PG-13", "R", "NC-17");
        allGenres = createGenres("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный");
    }

    @AfterEach
    void shutdown() {
        reviewStorage.deleteAll();
        filmGenreStorage.deleteAll();
        filmMpaStorage.deleteAll();
        filmStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    @DisplayName("Сохранение отзывов")
    void givenNewReview_whenSave_gotSaved() {
        User user = createUser();
        Film film = createFilm();

        FilmReview filmReview = reviewStorage.save(FilmReview.builder()
                .userId(user.getId())
                .filmId(film.getId())
                .isPositive(false)
                .content("This film is soo bad.")
                .build());

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview.getReviewId());

        assertReviewEquals(filmReview, actFilmReview);
        assertEquals(0, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Обновление отзывов")
    void givenExistingReview_whenSave_gotUpdated() {
        User user = createUser();
        Film film = createFilm();

        FilmReview filmReview = reviewStorage.save(FilmReview.builder()
                .userId(user.getId())
                .filmId(film.getId())
                .isPositive(false)
                .content("This film is soo bad.")
                .build());

        FilmReview updatedFilmReview = reviewStorage.save(FilmReview.builder()
                .reviewId(filmReview.getReviewId())
                .userId(user.getId())
                .filmId(film.getId())
                .content("This film is not too bad.")
                .isPositive(true)
                .build());

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview.getReviewId());

        assertReviewEquals(updatedFilmReview, actFilmReview);
        assertEquals(0, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Получение отзывов для определенного фильма")
    void givenReviews_whenGetByFilmAndCount_gotIt() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        createReviews(4, film1, createUser());
        createReviews(20, film2, createUser());

        List<FilmReview> filmReviews = reviewStorage.getAll(3, film1);
        assertEquals(3, filmReviews.size());

        filmReviews.forEach(review ->
            assertEquals(film1.getId(), review.getFilmId()));
    }

    @Test
    @DisplayName("Получение отзывов")
    void givenReviews_whenGetByCount_gotIt() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        createReviews(10, film1, createUser());
        createReviews(10, film2, createUser());

        List<FilmReview> filmReviews = reviewStorage.getAll(15);
        assertEquals(15, filmReviews.size());
    }

    @Test
    @DisplayName("Удаление отзыва")
    void givenExistingReviews_whenDeleteOne_gotDeleted() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        List<FilmReview> reviews1 = createReviews(2, film1, createUser());
        List<FilmReview> reviews2 = createReviews(1, film2, createUser());

        reviewStorage.delete(reviews1.get(0));

        List<FilmReview> actFilmReviews = reviewStorage.getAll(10);
        Set<Long> actReviewIds = actFilmReviews.stream().map(FilmReview::getReviewId).collect(Collectors.toSet());
        Set<Long> expReviewIds = Set.of(reviews1.get(1).getReviewId(), reviews2.get(0).getReviewId());

        assertEquals(expReviewIds, actReviewIds);
    }

    @Test
    @DisplayName("Удаление всех отзывов")
    void givenExistingReviews_whenDeleteAll_gotDeleted() {
        createReviews(2, createFilm(), createUser());
        createReviews(1, createFilm(), createUser());

        int deleted = reviewStorage.deleteAll();

        List<FilmReview> actFilmReviews = reviewStorage.getAll(10);

        assertEquals(3, deleted);
        assertTrue(actFilmReviews.isEmpty());
    }

    @Test
    @DisplayName("После удаления отзыва попытка его получить вызывает 404 ошибку")
    void givenDeletedReview_whenRequestIt_gotNotFound() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        List<FilmReview> reviews1 = createReviews(2, film1, createUser());
        List<FilmReview> reviews2 = createReviews(1, film2, createUser());

        reviewStorage.delete(reviews1.get(0));

        assertThrows(NotFoundException.class, () -> {
            getReviewByIdOrThrow(reviews1.get(0).getReviewId());
        });
    }

    @Test
    @DisplayName("Лайк увеличивает рейтинг отзыва на 1")
    void givenReview_whenAddLike_gotRateIncreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview1 = createReview(film, reviewAuthor);
        FilmReview filmReview2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addLikeToReview(filmReview1, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview1.getReviewId());

        assertEquals(1, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Повторный лайк от того же пользователя не изменяет рейтинг")
    void givenReview_whenAddLikeAgain_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addLikeToReview(filmReview, user);
        reviewStorage.addLikeToReview(filmReview, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview.getReviewId());

        assertEquals(1, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Дизлайк уменьшает рейтинг отзыва на 1")
    void givenLike_whenDeleteIt_gotRateDecreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview1 = createReview(film, reviewAuthor);
        FilmReview filmReview2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addLikeToReview(filmReview1, user);
        reviewStorage.deleteLikeToReview(filmReview1, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview1.getReviewId());

        assertEquals(0, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Удаление несуществующего лайка не уменьшает рейтинг отзыва")
    void givenNoLike_whenDelete_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview1 = createReview(film, reviewAuthor);
        FilmReview filmReview2 = createReview(film, reviewAuthor);

        User user1 = createUser();
        User user2 = createUser();

        reviewStorage.addLikeToReview(filmReview1, user1);
        reviewStorage.deleteLikeToReview(filmReview1, user2);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview1.getReviewId());

        assertEquals(1, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Дизлайк уменьшает рейтинг отзыва на 1")
    void givenReview_whenDislike_gotRateDecreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview1 = createReview(film, reviewAuthor);
        FilmReview filmReview2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addDislikeToReview(filmReview1, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview1.getReviewId());

        assertEquals(-1, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Повторный дизлайк от того же пользователя не изменяет рейтинг")
    void givenReview_whenDislikeAgain_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addDislikeToReview(filmReview, user);
        reviewStorage.addDislikeToReview(filmReview, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview.getReviewId());

        assertEquals(-1, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Удаление дизлайка увеличивает рейтинг на 1")
    void givenDislike_whenDeleteIt_gotRateIncreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview1 = createReview(film, reviewAuthor);
        FilmReview filmReview2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addDislikeToReview(filmReview1, user);
        reviewStorage.deleteDislikeToReview(filmReview1, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview1.getReviewId());

        assertEquals(0, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Удаление несуществующего дизлайка не изменяет рейтинг отзыва")
    void givenNoDislike_whenDelete_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview1 = createReview(film, reviewAuthor);
        FilmReview filmReview2 = createReview(film, reviewAuthor);

        User user1 = createUser();
        User user2 = createUser();

        reviewStorage.addDislikeToReview(filmReview1, user1);
        reviewStorage.deleteDislikeToReview(filmReview1, user2);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview1.getReviewId());

        assertEquals(-1, actFilmReview.getRate());
    }

    @Test
    @DisplayName("Замена лайка на дизлайк уменьшает рейтинг отзыва на 2")
    void givenLike_whenDislike_gotRateDecreasedByTwo() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        FilmReview filmReview = createReview(film, reviewAuthor);
        User user = createUser();

        reviewStorage.addLikeToReview(filmReview, user); // рейтинг +1
        reviewStorage.addDislikeToReview(filmReview, user);

        FilmReview actFilmReview = getReviewByIdOrThrow(filmReview.getReviewId());
        assertEquals(-1, actFilmReview.getRate());
    }

    @Test
    void givenLikedReviews_whenGet_gotReviewsRateOrdered() {
        List<Film> films = createFilms(2);
        List<User> users = createUsers(2);

        FilmReview filmReview1 = createReview(films.get(0), users.get(0));
        FilmReview filmReview2 = createReview(films.get(1), users.get(0));
        FilmReview filmReview3 = createReview(films.get(0), users.get(1));

        reviewStorage.addLikeToReview(filmReview1, users.get(1));
        reviewStorage.addLikeToReview(filmReview2, users.get(0));
        reviewStorage.addLikeToReview(filmReview2, users.get(1));

        List<FilmReview> filmReviews = reviewStorage.getAll(5);

        assertEquals(3, filmReviews.size());
        assertEquals(filmReview2.getReviewId(), filmReviews.get(0).getReviewId());
        assertEquals(filmReview1.getReviewId(), filmReviews.get(1).getReviewId());
        assertEquals(filmReview3.getReviewId(), filmReviews.get(2).getReviewId());
    }

    @Test
    void givenLikedReviews_whenGetByFilm_gotReviewsRateOrdered() {
        List<Film> films = createFilms(2);
        List<User> users = createUsers(2);

        FilmReview filmReview1 = createReview(films.get(0), users.get(0));
        FilmReview filmReview2 = createReview(films.get(1), users.get(0));
        FilmReview filmReview3 = createReview(films.get(0), users.get(1));

        reviewStorage.addLikeToReview(filmReview1, users.get(1));
        reviewStorage.addLikeToReview(filmReview2, users.get(0));
        reviewStorage.addLikeToReview(filmReview2, users.get(1));

        List<FilmReview> filmReviews = reviewStorage.getAll(5, films.get(0));

        assertEquals(2, filmReviews.size());
        assertEquals(filmReview1.getReviewId(), filmReviews.get(0).getReviewId());
        assertEquals(filmReview3.getReviewId(), filmReviews.get(1).getReviewId());
    }

    private List<User> createUsers(int count) {
        return IntStream.range(0, count).mapToObj(i -> createUser()).toList();
    }

    private User createUser() {
        return userStorage.save(TestUtil.getRandomUser());
    }

    private List<Film> createFilms(int count) {
        return IntStream.range(0, count).mapToObj(i -> createFilm()).toList();
    }

    private Film createFilm() {

        Film film = TestUtil.getRandomFilm();
        FilmMpa mpa = allMpa.get(rnd.nextInt(allMpa.size()));

        Set<FilmGenre> genres = new HashSet<>();
        FilmGenre genre1 = allGenres.get(rnd.nextInt(allGenres.size()));
        FilmGenre genre2 = allGenres.get(rnd.nextInt(allGenres.size()));
        genres.add(genre1);
        genres.add(genre2);

        film.setGenres(new ArrayList<>(genres));
        film.setMpa(mpa);

        return filmStorage.save(film);
    }

    private List<FilmReview> createReviews(int count, Film film, User user) {
        List<FilmReview> filmReviews = new ArrayList<>();
        for (int i = 0; i < count; i++) {
        filmReviews.add(createReview(film, user));
        }
        return filmReviews;
    }

    private FilmReview createReview(Film film, User user) {
        FilmReview filmReview = TestUtil.getRandomReview(film, user);
        return reviewStorage.save(filmReview);
    }

    private FilmReview getReviewByIdOrThrow(long id) {
        return reviewStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("отзыв не найден", "отзыв не найден по id = " + id));
    }

    private List<FilmMpa> createMpa(String... mpaNames) {
        return Arrays.stream(mpaNames)
                .map(mpaName -> filmMpaStorage.save(new FilmMpa(null, mpaName)))
                .toList();
    }

    private List<FilmGenre> createGenres(String... genreNames) {
        return Arrays.stream(genreNames)
                .map(genreName -> filmGenreStorage.save(new FilmGenre(null, genreName)))
                .toList();
    }
}
