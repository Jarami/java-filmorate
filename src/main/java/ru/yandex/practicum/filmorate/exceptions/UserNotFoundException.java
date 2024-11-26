package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super("не найден пользователь", "не найден пользователь с id = " + userId);
    }
}
