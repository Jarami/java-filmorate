package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping(value = {"", "/"})
    public Collection<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    // {
    // "name":"c0JttkwNlsgb7ez",
    // "description":"XZygrtk7d2UO4xXukuaxHvyLpkDNhDHkEGgUWQdVdFRrjaogV4",
    // "releaseDate":"1975-01-30",
    // "duration":70,
    // "mpa":{"id":3},
    // "genres":[{"id":5}]
    // }
    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@RequestBody NewFilmRequest newFilmRequest) {
        Film film = filmService.createFilm(newFilmRequest);
        return FilmMapper.mapToDto(film);
    }

    @PutMapping(value = {"", "/"})
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @DeleteMapping(value = {"", "/"})
    public int deleteAllFilms() {
        return filmService.deleteAllFilms();
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable long id) {
        filmService.deleteFilmById(id);
    }
}
