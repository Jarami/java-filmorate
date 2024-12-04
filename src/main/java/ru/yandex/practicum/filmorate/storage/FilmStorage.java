package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage extends AbstractStorage<Long, Film> {
    List<Film> getPopularFilms(int count);
    List<Film> getAllByIds(List<Long> ids);
}
