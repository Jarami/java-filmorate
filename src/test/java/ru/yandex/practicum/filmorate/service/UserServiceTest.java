package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.UserNotFound;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertEmpty;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertUserEquals;

public class UserServiceTest {

    UserStorage userStorage;
    UserService userService;

    @BeforeEach
    void setup() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Nested
    class CreateTests {
        @Test
        void givenUserCreateRequest_whenCreated_gotUser() {
            User userRequest = parseUser("my@email.com;login;name;2024-01-01");
            User user = userService.createUser(userRequest);

            assertNotNull(user.getId());
            assertUserEquals(userRequest, user);
            assertEmpty(user.getFriendsId());
        }

        @Test
        void givenUserCreateRequestWithoutName_whenCreated_gotUser() {
            User userRequest = parseUser("my@email.com;login;NULL;2024-01-01");
            User user = userService.createUser(userRequest);

            assertNotNull(user.getId());
            assertUserEquals(userRequest, user);
        }
    }

    @Nested
    class ReadTests {
        @Test
        void givenUsers_whenGetAll_gotUsers() {
            Map<String, User> users = new HashMap<>();
            users.put("login1", createUser("my1@email.com;login1;name1;2024-01-01"));
            users.put("login2", createUser("my2@email.com;login2;name2;2024-01-02"));

            Collection<User> actualUsers = userService.getAllUsers();

            actualUsers.forEach(actualUser -> {
                User user = users.get(actualUser.getLogin());
                assertUserEquals(user, actualUser);
            });
        }

        @Test
        void givenExistingUserId_whenGetById_gotUser() {
            User existingUser = createUser("my@email.com;login;name;2024-01-01");

            User user = userService.getUserById(existingUser.getId());

            assertEquals(existingUser.getId(), user.getId());
            assertUserEquals(existingUser, user);
        }
    }

    @Nested
    class UpdateTests {
        @Test
        void givenExistingUser_whenUpdate_gotUpdated() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
            User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

            User updatedUser = new User(user1.getId(), "my-new@email.com", "new-login", "new-name",
                    LocalDate.parse("2024-02-02"), List.of(user2));

            userService.updateUser(updatedUser);

            User actualUser = userService.getUserById(user1.getId());
            assertUserEquals(updatedUser, actualUser);
        }

        @Test
        void givenNonExistingUser_whenUpdate_gotError() {
            User user = new User(1L, "my@mail.ru", "login", "name", LocalDate.parse("2024-01-01"),
                    new ArrayList<>());

            assertThrows(UserNotFound.class, () -> userService.updateUser(user));
        }
    }

    @Nested
    class DeleteTests {
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
    }

    @Nested
    class FriendTests {
        @Test
        void givenExistingUsers_whenAddFriends_gotFriendship() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
            User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
            userService.addFriend(user1.getId(), user2.getId());

            Collection<User> user1friends = userService.getFriends(user1.getId());
            Collection<User> user2friends = userService.getFriends(user2.getId());

            assertTrue(user1friends.contains(user2));
            assertTrue(user2friends.contains(user1));
        }

        @Test
        void givenAbsentUser_whenAddFriends_gotNoFriendship() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");

            assertThrows(UserNotFound.class, () ->
                userService.addFriend(user1.getId() + 1, user1.getId()));

            assertThrows(UserNotFound.class, () ->
                    userService.addFriend(user1.getId(), user1.getId() + 1));
        }

        @Test
        void givenExistingUsers_whenRemoveFriends_gotNoFrienshipAnyMore() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
            User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
            userService.addFriend(user1.getId(), user2.getId());

            userService.removeFromFriends(user1.getId(), user2.getId());

            Collection<User> user1friends = userService.getFriends(user1.getId());
            Collection<User> user2friends = userService.getFriends(user2.getId());

            assertFalse(user1friends.contains(user2));
            assertFalse(user2friends.contains(user1));
        }

        @Test
        void givenUsersWithCommonFriends_whenGetCommon_gotThem() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
            User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
            User user3 = createUser("my3@email.com;login3;name3;2024-03-01");

            userService.addFriend(user1.getId(), user2.getId());
            userService.addFriend(user1.getId(), user3.getId());
            userService.addFriend(user2.getId(), user1.getId());
            userService.addFriend(user2.getId(), user3.getId());

            Collection<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());
            user3 = userService.getUserById(user3.getId());

            assertEquals(1, commonFriends.size());
            assertUserEquals(user3, commonFriends.iterator().next());
        }
    }

    private List<User> createAndSaveTestUsers() {

        List<User> users = new ArrayList<>();
        users.add(createUser("my1@email.com;login1;name1;2024-01-01"));
        users.add(createUser("my2@email.com;login2;name2;2024-02-01"));
        return users;
    }

    private User parseUser(String userString) {
        String[] chunks = userString.split(";");
        return new User(
                chunks[0],
                chunks[1].equals("NULL") ? null : chunks[1],
                chunks[2].equals("NULL") ? null : chunks[2],
                LocalDate.parse(chunks[3])
        );
    }

    private User createUser(String userString) {
        return userStorage.save(parseUser(userString));
    }
}
