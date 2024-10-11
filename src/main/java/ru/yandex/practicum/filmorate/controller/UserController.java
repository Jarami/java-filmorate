package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exceptions.NoUserFound;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserDao dao;

    @GetMapping(value = {"", "/"})
    public Collection<User> getAllUsers() {
        return dao.getAll();
    }

    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.info("creating user {}", user);
        setNameIfAbsent(user);
        dao.save(user);
        return user;
    }

    @PutMapping(value = {"", "/"})
    public User updateUser(@Valid @RequestBody User user) throws NoUserFound {
        log.info("updating user {}", user);
        checkUserId(user);
        dao.save(user);
        return user;
    }

    @DeleteMapping(value = {"", "/"})
    public int deleteUsers() {
        log.info("deleting all users");
        return dao.deleteAll();
    }

    private void setNameIfAbsent(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private void checkUserId(User user) throws NoUserFound {
        if (user.getId() == null || dao.getById(user.getId()) == null) {
            throw new NoUserFound("Не найден пользователь с логином " + user.getLogin() + " и id " + user.getId());
        }
    }
}
