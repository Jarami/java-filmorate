package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testCreateAndGetBadReview() {
        User user = createUser();
        Film film = createFilm();

        Review review = addReview(Review.builder()
                .userId(user.getId())
                .filmId(film.getId())
                .isPositive(false)
                .content("This film is soo bad.")
                .build());

        Review actReview = getReviewById(review.getId());

        assertReviewEquals(review, actReview);
    }

    @Test
    void testUpdateToPositive() {
        User user = createUser();
        Film film = createFilm();

        Review review = addReview(Review.builder()
                .userId(user.getId())
                .filmId(film.getId())
                .isPositive(false)
                .content("This film is soo bad.")
                .build());

        Review updatedReview = updateReview(Review.builder()
                .id(review.getId())
                .userId(user.getId())
                .filmId(film.getId())
                .content("This film is not too bad.")
                .isPositive(true)
                .build());

        Review actReview = getReviewById(review.getId());

        assertReviewEquals(updatedReview, actReview);
    }

    @Test
    void testGetReviewToFilm() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        createReviews(4, film1, createUser());
        createReviews(20, film2, createUser());

        List<Review> reviews = getAllReview(3, film1);
        assertEquals(3, reviews.size());

        reviews.forEach(review ->
            assertEquals(film1.getId(), review.getFilmId()));
    }

    @Test
    void testGetReviews() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        createReviews(10, film1, createUser());
        createReviews(10, film2, createUser());

        List<Review> reviews = getAllReview(15);
        assertEquals(15, reviews.size());
    }

    @Test
    void testGetReviewToFilmInCorrectOrder() {
        // TODO: проверить порядок
    }

    @Test
    void testDeleteReview() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        List<Review> reviews1 = createReviews(2, film1, createUser());
        List<Review> reviews2 = createReviews(1, film2, createUser());

        deleteReview(reviews1.get(0));

        List<Review> actReviews = getAllReview(10);
        Set<Long> actReviewIds = actReviews.stream().map(Review::getId).collect(Collectors.toSet());
        Set<Long> expReviewIds = Set.of(reviews1.get(1).getId(), reviews2.get(0).getId());

        assertEquals(expReviewIds, actReviewIds);
    }

    @Test
    void testGetReviewAfterDelete() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        List<Review> reviews1 = createReviews(2, film1, createUser());
        List<Review> reviews2 = createReviews(1, film2, createUser());

        deleteReview(reviews1.get(0));

        assertThrows(NotFoundException.class, () -> {
            getReviewById(reviews1.get(0).getId());
        });
    }

    private User createUser() {
        return userStorage.save(TestUtil.getRandomUser());
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
        return addReview(review);
    }

    private Review addReview(Review review) {
        return reviewStorage.save(review);
    }

    private Review updateReview(Review review) {
        return reviewStorage.save(review);
    }

    private Review getReviewById(long id) {
        return reviewStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("отзыв не найден", "отзыв не найден по id = " + id));
    }

    private List<Review> getAllReview(int count, Film film) {
        return reviewStorage.getAll(count, film);
    }

    private List<Review> getAllReview(int count) {
        return reviewStorage.getAll(count);
    }

    private void deleteReview(Review review) {
        reviewStorage.delete(review);
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
