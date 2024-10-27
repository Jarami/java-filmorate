package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.model.User;

@Getter
@RequiredArgsConstructor
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFound extends RuntimeException {
    private final User user;
}
