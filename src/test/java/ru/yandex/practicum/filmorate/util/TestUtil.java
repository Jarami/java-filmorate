package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtil {
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

    public static <T> void assertEmpty(Collection<T> collection) {
        assertTrue(collection.isEmpty(), "Collection must be empty, but has " + collection.size() + " elements");
    }

    public static <T> void assertEmpty(T[] array) {
        assertEquals(0, array.length, "Array must be empty, but has " + array.length + " elements");
    }
}
