package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    
    private final UserDao dao;

    public UserController(UserDao dao) {
        this.dao = dao;
    }

    @GetMapping(value = {"", "/"})
    public Collection<UserDto> getAllUsers() {
        return dao.getAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserDto userDto) {

        log.info("creating user {}", userDto);

        User user = fromDto(userDto);

        checkUser(user);
        setNameIfAbsent(user);

        dao.save(user);

        return UserMapper.toDto(user);
    }

    @PutMapping
    public UserDto updateUser(@RequestBody UserDto userDto) {

        log.info("updating user {}", userDto);

        User user = fromDto(userDto);

        checkId(user);
        checkUser(user);

        dao.save(user);

        return UserMapper.toDto(user);
    }

    private User fromDto(UserDto userDto) {
        return UserMapper.fromDto(userDto);
    }

    private void checkId(User user) {
        if (user.getId() == null || dao.getById(user.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя " + user.getLogin()
                    + " не существует");
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

    private void setNameIfAbsent(User user) {
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
