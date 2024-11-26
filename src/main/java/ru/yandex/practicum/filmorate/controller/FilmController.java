package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping(value = {"", "/"})
    public List<FilmDto> getAllFilms() {
        filmService.getAllFilms().forEach(film ->
                log.info("film = {}", film));

        return filmService.getAllFilms().stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable int id) {
        Film film = filmService.getFilmById(id);
        log.info("getting film {}", film);
        return FilmMapper.mapToDto(film);
    }

    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@RequestBody NewFilmRequest newFilmRequest) {
        Film film = filmService.createFilm(newFilmRequest);
        return FilmMapper.mapToDto(film);
    }

    @PutMapping(value = {"", "/"})
    public Film updateFilm(@RequestBody UpdateFilmRequest updateFilmRequest) {
        return filmService.updateFilm(updateFilmRequest);
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
