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
        DbReviewStorage.class, ReviewRowMapper.class,
        DbFilmMpaStorage.class, FilmMpaRowMapper.class})
public class DbReviewStorageTest {

    private final DbReviewStorage reviewStorage;
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

        Review review = reviewStorage.save(Review.builder()
                .userId(user.getId())
                .filmId(film.getId())
                .isPositive(false)
                .content("This film is soo bad.")
                .build());

        Review actReview = getReviewByIdOrThrow(review.getId());

        assertReviewEquals(review, actReview);
        assertEquals(0, actReview.getRate());
    }

    @Test
    @DisplayName("Обновление отзывов")
    void givenExistingReview_whenSave_gotUpdated() {
        User user = createUser();
        Film film = createFilm();

        Review review = reviewStorage.save(Review.builder()
                .userId(user.getId())
                .filmId(film.getId())
                .isPositive(false)
                .content("This film is soo bad.")
                .build());

        Review updatedReview = reviewStorage.save(Review.builder()
                .id(review.getId())
                .userId(user.getId())
                .filmId(film.getId())
                .content("This film is not too bad.")
                .isPositive(true)
                .build());

        Review actReview = getReviewByIdOrThrow(review.getId());

        assertReviewEquals(updatedReview, actReview);
        assertEquals(0, actReview.getRate());
    }

    @Test
    @DisplayName("Получение отзывов для определенного фильма")
    void givenReviews_whenGetByFilmAndCount_gotIt() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        createReviews(4, film1, createUser());
        createReviews(20, film2, createUser());

        List<Review> reviews = reviewStorage.getAll(3, film1);
        assertEquals(3, reviews.size());

        reviews.forEach(review ->
            assertEquals(film1.getId(), review.getFilmId()));
    }

    @Test
    @DisplayName("Получение отзывов")
    void givenReviews_whenGetByCount_gotIt() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        createReviews(10, film1, createUser());
        createReviews(10, film2, createUser());

        List<Review> reviews = reviewStorage.getAll(15);
        assertEquals(15, reviews.size());
    }

    @Test
    @DisplayName("Удаление отзыва")
    void givenExistingReviews_whenDeleteOne_gotDeleted() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        List<Review> reviews1 = createReviews(2, film1, createUser());
        List<Review> reviews2 = createReviews(1, film2, createUser());

        reviewStorage.delete(reviews1.get(0));

        List<Review> actReviews = reviewStorage.getAll(10);
        Set<Long> actReviewIds = actReviews.stream().map(Review::getId).collect(Collectors.toSet());
        Set<Long> expReviewIds = Set.of(reviews1.get(1).getId(), reviews2.get(0).getId());

        assertEquals(expReviewIds, actReviewIds);
    }

    @Test
    @DisplayName("Удаление всех отзывов")
    void givenExistingReviews_whenDeleteAll_gotDeleted() {
        createReviews(2, createFilm(), createUser());
        createReviews(1, createFilm(), createUser());

        int deleted = reviewStorage.deleteAll();

        List<Review> actReviews = reviewStorage.getAll(10);

        assertEquals(3, deleted);
        assertTrue(actReviews.isEmpty());
    }

    @Test
    @DisplayName("После удаления отзыва попытка его получить вызывает 404 ошибку")
    void givenDeletedReview_whenRequestIt_gotNotFound() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        List<Review> reviews1 = createReviews(2, film1, createUser());
        List<Review> reviews2 = createReviews(1, film2, createUser());

        reviewStorage.delete(reviews1.get(0));

        assertThrows(NotFoundException.class, () -> {
            getReviewByIdOrThrow(reviews1.get(0).getId());
        });
    }

    @Test
    @DisplayName("Лайк увеличивает рейтинг отзыва на 1")
    void givenReview_whenAddLike_gotRateIncreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review1 = createReview(film, reviewAuthor);
        Review review2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addLikeToReview(review1, user);

        Review actReview = getReviewByIdOrThrow(review1.getId());

        assertEquals(1, actReview.getRate());
    }

    @Test
    @DisplayName("Повторный лайк от того же пользователя не изменяет рейтинг")
    void givenReview_whenAddLikeAgain_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addLikeToReview(review, user);
        reviewStorage.addLikeToReview(review, user);

        Review actReview = getReviewByIdOrThrow(review.getId());

        assertEquals(1, actReview.getRate());
    }

    @Test
    @DisplayName("Дизлайк уменьшает рейтинг отзыва на 1")
    void givenLike_whenDeleteIt_gotRateDecreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review1 = createReview(film, reviewAuthor);
        Review review2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addLikeToReview(review1, user);
        reviewStorage.deleteLikeToReview(review1, user);

        Review actReview = getReviewByIdOrThrow(review1.getId());

        assertEquals(0, actReview.getRate());
    }

    @Test
    @DisplayName("Удаление несуществующего лайка не уменьшает рейтинг отзыва")
    void givenNoLike_whenDelete_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review1 = createReview(film, reviewAuthor);
        Review review2 = createReview(film, reviewAuthor);

        User user1 = createUser();
        User user2 = createUser();

        reviewStorage.addLikeToReview(review1, user1);
        reviewStorage.deleteLikeToReview(review1, user2);

        Review actReview = getReviewByIdOrThrow(review1.getId());

        assertEquals(1, actReview.getRate());
    }

    @Test
    @DisplayName("Дизлайк уменьшает рейтинг отзыва на 1")
    void givenReview_whenDislike_gotRateDecreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review1 = createReview(film, reviewAuthor);
        Review review2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addDislikeToReview(review1, user);

        Review actReview = getReviewByIdOrThrow(review1.getId());

        assertEquals(-1, actReview.getRate());
    }

    @Test
    @DisplayName("Повторный дизлайк от того же пользователя не изменяет рейтинг")
    void givenReview_whenDislikeAgain_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addDislikeToReview(review, user);
        reviewStorage.addDislikeToReview(review, user);

        Review actReview = getReviewByIdOrThrow(review.getId());

        assertEquals(-1, actReview.getRate());
    }

    @Test
    @DisplayName("Удаление дизлайка увеличивает рейтинг на 1")
    void givenDislike_whenDeleteIt_gotRateIncreased() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review1 = createReview(film, reviewAuthor);
        Review review2 = createReview(film, reviewAuthor);

        User user = createUser();

        reviewStorage.addDislikeToReview(review1, user);
        reviewStorage.deleteDislikeToReview(review1, user);

        Review actReview = getReviewByIdOrThrow(review1.getId());

        assertEquals(0, actReview.getRate());
    }

    @Test
    @DisplayName("Удаление несуществующего дизлайка не изменяет рейтинг отзыва")
    void givenNoDislike_whenDelete_gotNoRateChange() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review1 = createReview(film, reviewAuthor);
        Review review2 = createReview(film, reviewAuthor);

        User user1 = createUser();
        User user2 = createUser();

        reviewStorage.addDislikeToReview(review1, user1);
        reviewStorage.deleteDislikeToReview(review1, user2);

        Review actReview = getReviewByIdOrThrow(review1.getId());

        assertEquals(-1, actReview.getRate());
    }

    @Test
    @DisplayName("Замена лайка на дизлайк уменьшает рейтинг отзыва на 2")
    void givenLike_whenDislike_gotRateDecreasedByTwo() {
        User reviewAuthor = createUser();
        Film film = createFilm();
        Review review = createReview(film, reviewAuthor);
        User user = createUser();

        reviewStorage.addLikeToReview(review, user); // рейтинг +1
        reviewStorage.addDislikeToReview(review, user);

        Review actReview = getReviewByIdOrThrow(review.getId());
        assertEquals(-1, actReview.getRate());
    }

    @Test
    void givenLikedReviews_whenGet_gotReviewsRateOrdered() {
        List<Film> films = createFilms(2);
        List<User> users = createUsers(2);

        Review review1 = createReview(films.get(0), users.get(0));
        Review review2 = createReview(films.get(1), users.get(0));
        Review review3 = createReview(films.get(0), users.get(1));

        reviewStorage.addLikeToReview(review1, users.get(1));
        reviewStorage.addLikeToReview(review2, users.get(0));
        reviewStorage.addLikeToReview(review2, users.get(1));

        List<Review> reviews = reviewStorage.getAll(5);

        assertEquals(3, reviews.size());
        assertEquals(review2.getId(), reviews.get(0).getId());
        assertEquals(review1.getId(), reviews.get(1).getId());
        assertEquals(review3.getId(), reviews.get(2).getId());
    }

    @Test
    void givenLikedReviews_whenGetByFilm_gotReviewsRateOrdered() {
        List<Film> films = createFilms(2);
        List<User> users = createUsers(2);

        Review review1 = createReview(films.get(0), users.get(0));
        Review review2 = createReview(films.get(1), users.get(0));
        Review review3 = createReview(films.get(0), users.get(1));

        reviewStorage.addLikeToReview(review1, users.get(1));
        reviewStorage.addLikeToReview(review2, users.get(0));
        reviewStorage.addLikeToReview(review2, users.get(1));

        List<Review> reviews = reviewStorage.getAll(5, films.get(0));

        assertEquals(2, reviews.size());
        assertEquals(review1.getId(), reviews.get(0).getId());
        assertEquals(review3.getId(), reviews.get(1).getId());
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

    private List<Review> createReviews(int count, Film film, User user) {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < count; i++) {
        reviews.add(createReview(film, user));
        }
        return reviews;
    }

    private Review createReview(Film film, User user) {
        Review review = TestUtil.getRandomReview(film, user);
        return reviewStorage.save(review);
    }

    private Review getReviewByIdOrThrow(long id) {
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
