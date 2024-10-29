package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film save(Film film);

    Collection<Film> getAll();

    Film getById(long id);

    void delete(Film film);

    int deleteAll();
}
