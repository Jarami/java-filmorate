package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmLikeStorage;

import java.util.List;

@Service
public class LikeService {

    private final FilmService filmService;
    private final UserService userService;

    private final FilmLikeStorage filmLikeStorage;

    public LikeService(FilmService filmService, UserService userService,
                       @Qualifier("db") FilmLikeStorage filmLikeStorage) {

        this.filmService = filmService;
        this.userService = userService;
        this.filmLikeStorage = filmLikeStorage;

    }

    public boolean like(long filmId, long userId) {
        checkFilmId(filmId);
        checkUserId(userId);

        Film film = filmService.getFilmById(filmId);
        User user = userService.getUserById(userId);

        boolean result = filmLikeStorage.like(film, user);
        if (result) {
            film.setRate(film.getRate() + 1);
        }
        return result;
    }

    public boolean dislike(long filmId, long userId) {

        checkFilmId(filmId);
        checkUserId(userId);

        Film film = filmService.getFilmById(filmId);
        User user = userService.getUserById(userId);

        boolean result = filmLikeStorage.dislike(film, user);
        if (result) {
            film.setRate(film.getRate() - 1);
        }
        return result;
    }

    public List<Film> getPopularFilms(int count) {
        return filmService.getPopularFilms(count);
    }

    public List<Film> getPopularFilmsByYear(int count, int year) {
        return filmService.getPopularFilmsByYear(count, year);
    }

    public List<Film> getPopularFilmsByGenre(int count, int genreId) {
        return filmService.getPopularFilmsByGenre(count, genreId);
    }

    public List<Film> getPopularFilmsByYearAndGenre(int count, int year, int genreId) {
        return filmService.getPopularFilmsByYearAndGenre(count, year, genreId);
    }

    private void checkFilmId(Long filmId) {
        if (filmId == null) {
            throw new NotFoundException("не найден фильм", "не найден фильм по id = " + filmId);
        }
    }

    private void checkUserId(Long userId) {
        if (userId == null) {
            throw new NotFoundException("не найден пользователь", "не найден пользователь по id = " + userId);
        }
    }
}
