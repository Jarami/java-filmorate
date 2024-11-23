package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NotFoundException extends RuntimeException {

    private String description;

    public NotFoundException(String message, String description) {
        super(message);
        this.description = description;
    }
}
