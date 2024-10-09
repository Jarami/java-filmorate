package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.config.FilmConfig;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmConfig config;
    private final FilmDao dao;

    public FilmController(FilmConfig config, FilmDao dao) {
        this.config = config;
        this.dao = dao;
    }

    @GetMapping(value = {"", "/"})
    public Collection<FilmDto> getAllFilms() {
        return dao.getAll().stream()
                .map(FilmMapper::toDto)
                .toList();
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@RequestBody FilmDto filmDto) {

        log.info("creating film {}", filmDto);

        Film film = fromDto(filmDto);

        checkFilm(film);

        dao.save(film);
        return FilmMapper.toDto(film);
    }

    @PutMapping
    public FilmDto updateFilm(@RequestBody FilmDto filmDto) {

        log.info("updating film {}", filmDto);

        Film film = fromDto(filmDto);

        checkFilmId(film);
        checkFilm(film);

        dao.save(film);
        return FilmMapper.toDto(film);
    }

    private void checkFilmId(Film film) {
        if (film.getId() == null || dao.getById(film.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма " + film.getName() + " c id = "
                    + film.getId() + " не существует");
        }
    }

    private Film fromDto(FilmDto filmDto) {
        return FilmMapper.fromDto(filmDto);
    }

    private void checkFilm(Film film) {

        String name = film.getName();
        String description = film.getDescription();
        LocalDate releaseDate = film.getReleaseDate();
        int duration = film.getDuration();

        if (name == null || name.isEmpty()) {
            throw new ValidationException("Название фильма не должно быть пустым");
        }

        if (description != null && description.length() >= config.getMaxDescSize()) {
            throw new ValidationException("Описание фильма не должно быть больше, чем " + config.getMaxDescSize()
                    + " символов");
        }

        if (releaseDate != null && releaseDate.isBefore(config.getMinReleaseDate())) {
            throw new ValidationException("Релиз фильма не должен быть раньше " + config.getMinReleaseDate());
        }

        if (duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
