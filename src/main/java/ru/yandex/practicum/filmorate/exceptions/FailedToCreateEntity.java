package ru.yandex.practicum.filmorate.exceptions;

public class FailedToCreateEntity extends RuntimeException {
        public FailedToCreateEntity(String message) {
            super(message);
        }
}
