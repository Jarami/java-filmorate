package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmDao dao;

    @GetMapping(value = {"", "/"})
    public Collection<Film> getAllFilms() {
        return dao.getAll();
    }

    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@Valid @RequestBody Film film) {

        log.info("creating film {}", film);

        dao.save(film);
        return film;
    }

    @PutMapping(value = {"", "/"})
    public Film updateFilm(@Valid @RequestBody Film film) {

        log.info("updating film {}", film);

        checkFilmId(film);

        dao.save(film);
        return film;
    }

    @DeleteMapping(value = {"", "/"})
    public int deleteAllFilms() {
        log.info("deleting all films");
        return dao.deleteAll();
    }

    private void checkFilmId(Film film) {
        if (film.getId() == null || dao.getById(film.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма " + film.getName() + " c id = "
                    + film.getId() + " не существует");
        }
    }
}
