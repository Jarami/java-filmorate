package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmFriendshipStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertEmpty;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertUserEquals;

// Эти тесты не учитывают валидации

public class UserServiceTest {

    UserStorage userStorage;
    UserService userService;
    FriendshipStorage friendshipStorage;

    @BeforeEach
    void setup() {
        userStorage = new InMemoryUserStorage();
        friendshipStorage = new InMemoryFilmFriendshipStorage();
        userService = new UserService(userStorage, friendshipStorage);
    }

    @Nested
    class CreateTests {
        @Test
        void givenUserCreateRequest_whenCreated_gotUser() {
            NewUserRequest userRequest = parseUser("my@email.com;login;name;2024-01-01");
            User user = userService.createUser(userRequest);

            assertNotNull(user.getId());
            assertUserEquals(UserMapper.mapToUser(userRequest), user);
        }

        @Test
        void givenUserCreateRequestWithoutName_whenCreated_gotLoginInsteadOfName() {
            NewUserRequest userRequest = parseUser("my@email.com;login;NULL;2024-01-01");
            User user = userService.createUser(userRequest);

            assertNotNull(user.getId());
            assertUserEquals(UserMapper.mapToUser(userRequest), user);
        }
    }

    @Nested
    class ReadTests {
        @Test
        void givenUsers_whenGetAll_gotUsers() {
            Map<String, User> users = new HashMap<>();
            users.put("login1", createUser("my1@email.com;login1;name1;2024-01-01"));
            users.put("login2", createUser("my2@email.com;login2;name2;2024-01-02"));

            List<User> actualUsers = userService.getAllUsers();

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

        @Test
        void givenNonExistingUserId_whenGetById_gotNotFound() {
            User user = new User(1L, "my@mail.ru", "login", "name", LocalDate.parse("2024-01-01"));

            assertThrows(NotFoundException.class, () -> userService.getUserById(user.getId()));
        }
    }

    @Nested
    class UpdateTests {
        @Test
        void givenExistingUser_whenUpdate_gotUpdated() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
            User user2 = createUser("my2@email.com;login2;name2;2024-02-01");

            UpdateUserRequest updatedUser = UpdateUserRequest.builder()
                    .id(user1.getId())
                    .email("my-new@email.com")
                    .login("new-login")
                    .name("new-name")
                    .build();

            userService.updateUser(updatedUser);

            User actualUser = userService.getUserById(user1.getId());
            assertUserEquals(UserMapper.mapToUser(updatedUser), actualUser);
        }

        @Test
        void givenNonExistingUser_whenUpdate_gotError() {
            UpdateUserRequest user = UpdateUserRequest.builder()
                            .id(1L)
                            .email("my@mail.ru")
                            .login("login")
                            .name("name")
                            .birthday(LocalDate.parse("2024-01-01"))
                            .build();

            assertThrows(NotFoundException.class, () -> userService.updateUser(user));
        }

        @Test
        void givenUserWithoutName_whenUpdate_gotLoginInsteadOfName() {
            User user = createUser("my@email.com;login;name;2024-01-01");

            UpdateUserRequest updatedUser = UpdateUserRequest.from(user)
                    .name(null)
                    .build();

            user = userService.updateUser(updatedUser);

            assertEquals("login", user.getName());
        }
    }

    @Nested
    class DeleteTests {
        @Test
        void givenUsers_whenDeleteAll_gotDeleted() {
            createAndSaveTestUsers();

            userService.deleteAllUsers();

            assertEmpty(userService.getAllUsers());
        }

        @Test
        void givenUser_whenDelete_gotDeleted() {
            List<User> users = createAndSaveTestUsers();

            userService.deleteUserById(users.getFirst().getId());

            List<User> actualUsers = userService.getAllUsers();
            List<User> expectedUsers = List.of(users.get(1));

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

            List<User> user1friends = userService.getFriends(user1.getId());
            List<User> user2friends = userService.getFriends(user2.getId());

            assertTrue(user1friends.contains(user2));
            assertTrue(user2friends.contains(user1));
        }

        @Test
        void givenAbsentUser_whenAddFriends_gotNoFriendship() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");

            assertThrows(NotFoundException.class, () ->
                userService.addFriend(user1.getId() + 1, user1.getId()));

            assertThrows(NotFoundException.class, () ->
                    userService.addFriend(user1.getId(), user1.getId() + 1));
        }

        @Test
        void givenExistingUsers_whenRemoveFriends_gotNoFrienshipAnyMore() {
            User user1 = createUser("my1@email.com;login1;name1;2024-01-01");
            User user2 = createUser("my2@email.com;login2;name2;2024-02-01");
            userService.addFriend(user1.getId(), user2.getId());

            userService.removeFromFriends(user1.getId(), user2.getId());

            List<User> user1friends = userService.getFriends(user1.getId());
            List<User> user2friends = userService.getFriends(user2.getId());

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

            List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());
            user3 = userService.getUserById(user3.getId());

            assertEquals(1, commonFriends.size());
            assertUserEquals(user3, commonFriends.getFirst());
        }
    }

    private List<User> createAndSaveTestUsers() {

        List<User> users = new ArrayList<>();
        users.add(createUser("my1@email.com;login1;name1;2024-01-01"));
        users.add(createUser("my2@email.com;login2;name2;2024-02-01"));
        return users;
    }

    private NewUserRequest parseUser(String userString) {
        String[] chunks = userString.split(";");
        return NewUserRequest.builder()
                .email(chunks[0])
                .login(chunks[1].equals("NULL") ? null : chunks[1])
                .name(chunks[2].equals("NULL") ? null : chunks[2])
                .birthday(chunks[3].equals("NULL") ? null : LocalDate.parse(chunks[3]))
                .build();
    }

    private User createUser(String userString) {
        NewUserRequest newUserRequest = parseUser(userString);
        return userStorage.save(UserMapper.mapToUser(newUserRequest));
    }
}
