package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage extends AbstractStorage<Long, Film> {
    List<Film> getPopularFilms(int count);

    List<Film> getRecommendations(long userId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getPopularFilmsByYear(int count, int year);

    List<Film> getPopularFilmsByGenre(int count, int genre);

    List<Film> getPopularFilmsByYearGenre(int count, int year, int genre);
}
