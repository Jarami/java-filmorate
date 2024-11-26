package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.BadRequestException;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotFound(BadRequestException e) {

        log.error("Плохой запрос", e);

        return new ErrorResponse(e.getMessage(), e.getDescription());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {

        log.error("Не найдено", e);

        return new ErrorResponse(e.getMessage(), e.getDescription());
    }

//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ErrorResponse handleUserNotFound(UserNotFoundException e) {
//
//        log.error("Пользователь с id = {} не найден", e.getUserId(), e);
//
//        return new ErrorResponse(
//            "Пользователь не найден",
//            "Пользователь с id = " + e.getUserId() + " не найден"
//        );
//    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleFilmNotFound(FilmNotFoundException e) {

        log.error("Фильм с id = {} не найден", e.getFilmId(), e);

        return new ErrorResponse(
                "Фильм не найден",
                "Фильм с id = " + e.getFilmId() + " не найден"
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstrainViolation(ConstraintViolationException e) {

        log.error("Ошибка валидации", e);

        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String description = violations.stream()
                .map(cv -> cv == null ? "null" : cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        return new ErrorResponse("Ошибка валидации", description);
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        log.error("Ошибка валидации", e);

        String description = e.getBindingResult().getFieldErrors().stream()
                .map(ex -> ex.getField() + ": " + ex.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ErrorResponse("Ошибка валидации", description);
    }
}
