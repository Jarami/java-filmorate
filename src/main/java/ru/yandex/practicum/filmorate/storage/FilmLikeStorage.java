package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

public interface FilmLikeStorage {
    public boolean like(Film film, User user);
    public boolean dislike(Film film, User user);
}
