package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.BadRequestException;
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

        return filmService.getAllFilms().stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@PathVariable Integer id) {
        Film film = filmService.getFilmById(id);
        return FilmMapper.mapToDto(film);
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(@RequestParam(required = false) String query,
                                     @RequestParam(required = false) String by) {

        List<Film> films = filmService.searchFilms(query, by);

        return films.stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    @GetMapping(value = "/common")
    public List<FilmDto> getCommonFilms(@RequestParam Long userId,
                                        @RequestParam Long friendId) {

        return filmService.getCommonFilms(userId, friendId).stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getSortedFilmsByDirector(@PathVariable Integer directorId,
                                                  @RequestParam String sortBy) {

        if (!sortBy.equalsIgnoreCase("year") && !sortBy.equalsIgnoreCase("likes")) {
            throw new BadRequestException("Некорректный режим сортировки", "Нет сортировки для sortBy={}" + sortBy);
        }

        return filmService.getSortedFilmsByDirector(directorId, sortBy).stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@RequestBody NewFilmRequest newFilmRequest) {
        Film film = filmService.createFilm(newFilmRequest);
        return FilmMapper.mapToDto(film);
    }

    @PutMapping(value = {"", "/"})
    public FilmDto updateFilm(@RequestBody UpdateFilmRequest updateFilmRequest) {
        Film film = filmService.updateFilm(updateFilmRequest);
        return FilmMapper.mapToDto(film);
    }

    @DeleteMapping(value = {"", "/"})
    public int deleteAllFilms() {
        return filmService.deleteAllFilms();
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Long id) {
        filmService.deleteFilmById(id);
    }
}
