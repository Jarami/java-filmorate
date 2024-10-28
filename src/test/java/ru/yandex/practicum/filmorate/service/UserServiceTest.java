package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.UserNotFound;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.*;

public class UserServiceTest {

    UserStorage userStorage;
    UserService userService;

    @BeforeEach
    void setup() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Test
    void givenUserCreateRequest_whenCreated_gotUser() {
        User userRequest = new User("my@email.com", "login", "name", LocalDate.parse("2024-01-01"));

        User user = userService.createUser(userRequest);

        assertNotNull(user.getId());
        assertUserEquals(userRequest, user);
        assertEmpty(user.getFriendsId());
    }

    @Test
    void givenUserCreateRequestWithoutName_whenCreated_gotUser() {
        User userRequest = new User("my@email.com", "login", null, LocalDate.parse("2024-01-01"));

        User user = userService.createUser(userRequest);

        assertNotNull(user.getId());
        assertUserEquals(userRequest, user);
    }

    @Test
    void givenUsers_whenGetAll_gotUsers() {
        Collection<User> users = new ArrayList<>();
        users.add(userStorage.save(new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01"))));
        users.add(userStorage.save(new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-01-02"))));

        Map<Integer, User> actualUserById = userService.getAllUsers().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        users.forEach(user -> {
            User actualUser = actualUserById.get(user.getId());
            assertUserEquals(user, actualUser);
        });
    }

    @Test
    void givenExistingUserId_whenGetById_gotUser() {
        User userRequest = new User("my@email.com", "login", "name", LocalDate.parse("2024-01-01"));
        User existingUser = userStorage.save(userRequest);

        User user = userService.getUserById(existingUser.getId());

        assertEquals(existingUser.getId(), user.getId());
        assertUserEquals(existingUser, user);
    }

    @Test
    void givenExistingUser_whenUpdate_gotUpdated() {
        User user1 = userStorage.save(new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01")));
        User user2 = userStorage.save(new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-02-01")));

        User updatedUser = new User(user1.getId(), "my-new@email.com", "new-login", "new-name",
                LocalDate.parse("2024-02-02"), List.of(user2));

        userService.updateUser(updatedUser);

        User actualUser = userService.getUserById(user1.getId());
        assertUserEquals(updatedUser, actualUser);
    }

    @Test
    void givenNonExistingUser_whenUpdate_gotError() {
        User user = new User(1, "my@mail.ru", "login", "name", LocalDate.parse("2024-01-01"),
                new ArrayList<>());

        assertThrows(UserNotFound.class, () -> userService.updateUser(user));
    }

    @Test
    void givenUsers_whenDeleteAll_gotDeleted() {
        List<User> users = createAndSaveTestUsers();

        userService.deleteAllUsers();

        assertEmpty(userService.getAllUsers());
    }

    @Test
    void givenUser_whenDelete_gotDeleted() {
        List<User> users = createAndSaveTestUsers();

        userService.deleteUser(users.getFirst());

        Collection<User> actualUsers = userService.getAllUsers();
        Collection<User> expectedUsers = List.of(users.get(1));

        assertIterableEquals(expectedUsers, actualUsers);
    }

    private List<User> createAndSaveTestUsers() {

        List<User> users = new ArrayList<>();
        users.add(userStorage.save(
                new User("my1@email.com", "login1", "name1", LocalDate.parse("2024-01-01"))));

        users.add(userStorage.save(
                new User("my2@email.com", "login2", "name2", LocalDate.parse("2024-02-01"))));

        return users;
    }
}
