package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    
    private UserDao dao;

    public UserController(UserDao dao) {
        this.dao = dao;
    }

    @GetMapping(value = {"", "/"})
    public Collection<User> getAllUsers() {
        return dao.getAll();
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {

        log.info("creating user {}", user);

        checkUser(user);

        dao.save(user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {

        log.info("updating user {}", user);

        checkId(user);
        checkUser(user);
        setNameIfabsent(user);

        dao.save(user);
        return user;
    }

    private void checkId(User user) {
        if (user.getId() == null || dao.getById(user.getId()) == null) {
            throw new ValidationException("user " + user + " has no id");
        }
    }

    private void checkUser(User user) {
        String email = user.getEmail();
        String login = user.getLogin();
        LocalDate birthday = user.getBirthday();
        LocalDate now = LocalDate.now();

        if (email == null || email.isEmpty()) {
            throw new ValidationException("Почта пользователя должна быть заполнена");
        } else if (!email.contains("@")) {
            throw new ValidationException("Почта пользователя должна содержать @");
        }

        if (login == null || login.isEmpty()) {
            throw new ValidationException("Логин пользователя должен быть заполнен");
        } else if (containsWhitespace(login)) {
            throw new ValidationException("Логин пользователя не должен содержать пробельные символы");
        }

        if (birthday.isAfter(now)) {
            throw new ValidationException("День рождения пользователя не может быть в будущем");
        }
    }

    private void setNameIfabsent(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
