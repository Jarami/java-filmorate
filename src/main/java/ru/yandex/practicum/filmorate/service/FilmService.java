package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFound;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    @Validated(Marker.OnCreate.class)
    public Film createFilm(@Valid Film film) {
        log.info("creating film {}", film);
        filmStorage.save(film);
        return film;
    }

    public Collection<Film> getAllFilms() {
        log.info("getting all films");
        return filmStorage.getAll();
    }

    public Film getFilmById(long id) {
        checkFilmId(id);
        return filmStorage.getById(id);
    }

    @Validated(Marker.OnUpdate.class)
    public Film updateFilm(@Valid Film film) {
        log.info("updating film {}", film);
        checkFilmId(film.getId());
        filmStorage.save(film);
        return film;
    }

    public void deleteFilmById(long filmId) {
        log.info("deleting film with id = {}", filmId);
        checkFilmId(filmId);
        filmStorage.delete(filmStorage.getById(filmId));
    }

    public int deleteAllFilms() {
        log.info("deleting all films");
        return filmStorage.deleteAll();
    }

    private void checkFilmId(Long filmId) {
        if (filmId == null || filmStorage.getById(filmId) == null) {
            throw new FilmNotFound(filmId);
        }
    }
}
