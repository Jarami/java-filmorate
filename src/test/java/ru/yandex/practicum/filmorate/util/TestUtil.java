package ru.yandex.practicum.filmorate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Random;

import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

public class TestUtil {

    private static final Random random = new Random();

    public static void assertUserEquals(User expectedUser, User actualUser) {
        assertNotNull(actualUser);
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getLogin(), actualUser.getLogin());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getBirthday(), actualUser.getBirthday());
    }

    public static void assertUserEquals(NewUserRequest expectedUser, User actualUser) {
        assertNotNull(actualUser);
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getLogin(), actualUser.getLogin());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getBirthday(), actualUser.getBirthday());
    }

    public static void assertUserEquals(UpdateUserRequest expectedUser, User actualUser) {
        assertNotNull(actualUser);
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
        assertEquals(expectedUser.getLogin(), actualUser.getLogin());
        assertEquals(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getBirthday(), actualUser.getBirthday());
    }

    public static void assertFilmEquals(Film expectedFilm, Film actualFilm) {
        assertNotNull(actualFilm);
        assertEquals(expectedFilm.getName(), actualFilm.getName());
        assertEquals(expectedFilm.getDescription(), actualFilm.getDescription());
        assertEquals(expectedFilm.getReleaseDate(), actualFilm.getReleaseDate());
        assertEquals(expectedFilm.getDuration(), actualFilm.getDuration());
    }

    public static void assertFilmEquals(NewFilmRequest expectedFilm, Film actualFilm) {
        assertNotNull(actualFilm);
        assertEquals(expectedFilm.getName(), actualFilm.getName());
        assertEquals(expectedFilm.getDescription(), actualFilm.getDescription());
        assertEquals(expectedFilm.getReleaseDate(), actualFilm.getReleaseDate());
        assertEquals(expectedFilm.getDuration(), actualFilm.getDuration());
    }

    public static void assertFilmEquals(UpdateFilmRequest expectedFilm, Film actualFilm) {
        assertNotNull(actualFilm);
        assertEquals(expectedFilm.getName(), actualFilm.getName());
        assertEquals(expectedFilm.getDescription(), actualFilm.getDescription());
        assertEquals(expectedFilm.getReleaseDate(), actualFilm.getReleaseDate());
        assertEquals(expectedFilm.getDuration(), actualFilm.getDuration());
    }

    public static void assertReviewEquals(Review expReview, Review actReview) {
        assertNotNull(actReview);
        assertEquals(expReview.getId(), actReview.getId());
        assertEquals(expReview.getUserId(), actReview.getUserId());
        assertEquals(expReview.getFilmId(), actReview.getFilmId());
        assertEquals(expReview.getContent(), actReview.getContent());
        assertEquals(expReview.isPositive(), actReview.isPositive());
    }

    public static <T> void assertEmpty(Collection<T> collection) {
        assertTrue(collection.isEmpty(), "Collection must be empty, but has " + collection.size() + " elements");
    }

    public static <T> void assertEmpty(T[] array) {
        assertEquals(0, array.length, "Array must be empty, but has " + array.length + " elements");
    }

    public static Film getRandomFilm() {

        int year = 2020;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        int duration = random.nextInt(100) + 100;

        return Film.builder()
            .name(randomString(10))
            .description(randomString(50))
            .releaseDate(LocalDate.of(year, month, day))
            .duration(duration)
            .build();
    }

    public static User getRandomUser() {

        int year = 2020;
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;

        return User.builder()
            .email(randomString(10) + "@mail.ru")
            .login(randomString(20))
            .name(randomString(10) + " " + randomString(10))
            .birthday(LocalDate.of(year, month, day))
            .build();
    }

    public static Review getRandomReview(Film film, User user) {
        return Review.builder()
                .filmId(film.getId())
                .userId(user.getId())
                .content(randomString(100))
                .isPositive(true)
                .rate(0)
                .build();
    }

    public static String randomString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
