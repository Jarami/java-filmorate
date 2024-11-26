package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super("не найден пользователь", "не найден пользователь с id = " + userId);
    }
}
