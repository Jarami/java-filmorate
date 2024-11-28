package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

public interface FilmLikeStorage {

    boolean like(Film film, User user);

    boolean dislike(Film film, User user);

    int deleteAll();
}
