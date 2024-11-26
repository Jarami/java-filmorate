package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertUserEquals;

class InMemoryUserStorageTest {

    InMemoryUserStorage storage;

    @BeforeEach
    void setup() {
        storage = new InMemoryUserStorage();
    }

    @Test
    void givenNewUser_whenSave_gotSaved() {
        User user = new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01"));

        User savedUser = storage.save(user);

        assertUserEquals(savedUser, user);
    }

    @Test
    void givenOldUser_whenSave_gotUpdated() {
        User savedUser = storage.save(
                new User("my@email.com", "login", "name", LocalDate.parse("2024-01-01")));

        long userId = savedUser.getId();

        User updatedUser = storage.save(new User(userId, "my2@email.com", "login2",
                "name2", LocalDate.parse("2024-02-01")));

        User actualUpdatedUser = storage.getById(savedUser.getId())
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь с id = " + userId));

        assertUserEquals(updatedUser, actualUpdatedUser);
    }

    @Test
    void givenSavedUsers_whenGetAll_gotAll() {
        User user1 = storage.save(
                new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01")));
        User user2 = storage.save(
                new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-02-01")));

        Map<Long, User> users = storage.getAll().stream().collect(Collectors.toMap(User::getId, Function.identity()));

        assertEquals(2, users.size());
        assertUserEquals(user1, users.get(user1.getId()));
        assertUserEquals(user2, users.get(user2.getId()));
    }

    @Test
    void givenSavedUsers_whenGetOneById_gotIt() {
        User user1 = storage.save(
                new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01")));
        User user2 = storage.save(
                new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-02-01")));

        long userId = user1.getId();

        User actualUser = storage.getById(user1.getId())
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь с id = " + userId));

        assertUserEquals(user1, actualUser);
    }

    @Test
    void givenSavedUsers_whenGetOneByWrongId_gotNull() {
        User user1 = storage.save(
                new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01")));

        long userId = user1.getId() + 1;

        User actualUser = storage.getById(user1.getId() + 1)
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь с id = " + userId));

        assertNull(actualUser);
    }

    @Test
    void givenExistingUser_whenDelete_gotDeleted() {
        User user1 = storage.save(
                new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01")));
        User user2 = storage.save(
                new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-02-01")));

        storage.delete(user1);

        assertNull(storage.getById(user1.getId()));
        assertNotNull(storage.getById(user2.getId()));
    }

    @Test
    void givenUsers_whenDeleteAll_gotDeleted() {
        User user1 = storage.save(
                new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01")));
        User user2 = storage.save(
                new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-02-01")));

        int deleted = storage.deleteAll();

        assertTrue(storage.getAll().isEmpty());
        assertEquals(2, deleted);
    }
}