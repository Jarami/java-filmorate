package ru.yandex.practicum.filmorate.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.util.TestUtil;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ DbUserStorage.class, UserRowMapper.class })
public class DbUserStorageTest {

    private final DbUserStorage userStorage;

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        shutdown();

        user1 = createUser();
        user2 = createUser();
    }

    @AfterEach
    void shutdown() {
        userStorage.deleteAll();
    }

    @Test
    void givenUser_whenDelete_gotDeleted() {
        userStorage.delete(user1);
        List<User> actUsers = userStorage.getAll();
        assertEquals(1, actUsers.size());
        assertEquals(user2.getName(), actUsers.get(0).getName());
    }

    @Test
    void givenUsers_whenFetch_gotAll() {
        List<User> actUsers = userStorage.getAll();

        assertEquals(2, actUsers.size());
    }

    @Test
    void givenUser_whenFetch_gotIt() {
        User actUser = userStorage.getById(user1.getId()).get();

        assertEquals(user1.getName(), actUser.getName());
    }

    @Test
    void givenUser_whenUpdate_gotUpdated() {
        user1.setName("user");
        user1.setEmail("mail@main.ru");
        user1.setLogin("login");
        user1.setBirthday(LocalDate.parse("1999-01-01"));
        log.info("user1 = {}", user1);
        userStorage.save(user1);
        log.info("user1 = {}", user1);

        User actUser = userStorage.getById(user1.getId()).get();

        log.info("actUser = {}", actUser);
        assertEquals("user", actUser.getName());
        assertEquals("mail@main.ru", actUser.getEmail());
        assertEquals("login", actUser.getLogin());
        assertEquals("1999-01-01", actUser.getBirthday().toString());
    }

    private User createUser() {
        return userStorage.save(TestUtil.getRandomUser());
    }
}
