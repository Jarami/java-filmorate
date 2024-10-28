package ru.yandex.practicum.filmorate.util;

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
        assertIterableEquals(expectedUser.getFriendsId(), actualUser.getFriendsId());
    }

    public static <T> void assertEmpty(Collection<T> collection) {
        assertTrue(collection.isEmpty());
    }

    public static <T> void assertNotEmpty(Collection<T> collection) {
        assertFalse(collection.isEmpty());
    }
}
