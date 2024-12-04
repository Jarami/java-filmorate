package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;
import java.util.Set;

public interface FilmLikeStorage {

    Map<Long, Set<Long>> getLikes();

    boolean like(Film film, User user);

    boolean dislike(Film film, User user);

    int deleteAll();
}
