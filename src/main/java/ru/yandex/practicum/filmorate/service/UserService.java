package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.UserNotFound;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(@Valid User user) {
        log.info("creating user {}", user);
        setNameIfAbsent(user);
        userStorage.save(user);
        return user;
    }

    public Collection<User> getAllUsers() {
        log.info("getting all user");
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        return userStorage.getById(id);
    }

    public User updateUser(@Valid User user) {
        log.info("updating user {}", user);
        checkUserId(user);
        userStorage.save(user);
        return user;
    }

    public void deleteUser(@Valid User user) {
        log.info("deleting user {}", user);
        checkUserId(user);
        userStorage.delete(user);
    }

    public int deleteAllUsers() {
        log.info("deleting all users");
        return userStorage.deleteAll();
    }

    public void makeFriends(User user, User friend) {

    }

    public void removeFromFriends(User user, User friend) {

    }

    public Collection<User> getCommonFriends(User user, User otherUser) {
        throw new RuntimeException("not implemented");
    }

    private void setNameIfAbsent(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private void checkUserId(User user) {
        if (user.getId() == null || userStorage.getById(user.getId()) == null) {
            throw new UserNotFound(user);
        }
    }
}
