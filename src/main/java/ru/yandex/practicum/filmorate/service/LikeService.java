package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final FilmService filmService;
    private final UserService userService;

    public Film like(long filmId, long userId) {
        checkFilmId(filmId);
        checkUserId(userId);

        Film film = filmService.getFilmById(filmId);

        film.addLike(userId);

        return film;
    }

    public Film dislike(long filmId, long userId) {
        checkFilmId(filmId);
        checkUserId(userId);

        Film film = filmService.getFilmById(filmId);

        film.removeLike(userId);

        return film;
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
