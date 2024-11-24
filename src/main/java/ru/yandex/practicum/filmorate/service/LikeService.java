package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
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

    public FilmDto like(long filmId, long userId) {
        checkFilmId(filmId);
        checkUserId(userId);

        Film film = filmService.getFilmById(filmId);
        User user = userService.getUserById(userId);

        filmLikeStorage.like(film, user);

        return FilmMapper.mapToDto(film);
    }

    public FilmDto dislike(long filmId, long userId) {

        checkFilmId(filmId);
        checkUserId(userId);

        Film film = filmService.getFilmById(filmId);
        User user = userService.getUserById(userId);

        filmLikeStorage.dislike(film, user);

        return FilmMapper.mapToDto(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmService.getAllFilms()
                .stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikeCount(), film1.getLikeCount()))
                .limit(count)
                .toList();
    }

    private void checkFilmId(Long filmId) {
        if (filmId == null || filmService.getFilmById(filmId) == null) {
            throw new FilmNotFoundException(filmId);
        }
    }

    private void checkUserId(Long userId) {
        if (userId == null || userService.getUserById(userId) == null) {
            throw new UserNotFoundException(userId);
        }
    }
}
