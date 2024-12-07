package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.BadRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.FilmMpaStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Validated
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmMpaStorage filmMpaStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final UserService userService;
    private final DirectorService directorService;

    public FilmService(
            @Qualifier("db") FilmStorage filmStorage,
            @Qualifier("db") FilmMpaStorage filmMpaStorage,
            @Qualifier("db") FilmGenreStorage filmGenreStorage,
            UserService userService, DirectorService directorService) {

        this.filmStorage = filmStorage;
        this.filmMpaStorage = filmMpaStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.userService = userService;
        this.directorService = directorService;
    }

    public Film createFilm(@Valid NewFilmRequest newFilmRequest) {

        log.info("creating film {}", newFilmRequest);

        Film film = FilmMapper.mapToFilm(newFilmRequest);
        film.setMpa(getFilmMpa(newFilmRequest));

        if (newFilmRequest.getGenres() != null) {
            List<FilmGenre> genres = getFilmGenres(newFilmRequest);
            if (genres.isEmpty()) {
                throw new BadRequestException("неуспешный запрос", "пустой список жанров");
            }
            film.setGenres(genres);
        }

        if (newFilmRequest.getDirectors() != null) {
            if (!newFilmRequest.getDirectors().isEmpty()) {
                List<Director> directors = getFilmDirectors(newFilmRequest);
                directorService.validateDirectorsCreateAndUpdate(directors, newFilmRequest.getDirectors().size());
                film.setDirectors(directors);
            } else {
                log.debug("В запросе пришёл пустой список режиссеров");
                film.setDirectors(new ArrayList<>());
            }
        }

        log.info("saving film {}", film);
        return filmStorage.save(film);
    }

    private FilmMpa getFilmMpa(NewFilmRequest newFilmRequest) {
        Integer mpaId = newFilmRequest.getMpa().getId();

        return filmMpaStorage.getById(mpaId)
                .orElseThrow(() ->
                        new BadRequestException("не найден рейтинг", "не найден рейтинг по id " + mpaId));
    }

    private FilmMpa getFilmMpa(UpdateFilmRequest updateFilmRequest) {
        Integer mpaId = updateFilmRequest.getMpa().getId();

        return filmMpaStorage.getById(mpaId)
                .orElseThrow(() ->
                        new NotFoundException("не найден рейтинг", "не найден рейтинг по id " + mpaId));
    }

    private List<FilmGenre> getFilmGenres(NewFilmRequest newFilmRequest) {
        List<Integer> ids = newFilmRequest.getGenres().stream().map(FilmGenreDto::getId).toList();
        return filmGenreStorage.getById(ids);
    }

    private List<Director> getFilmDirectors(NewFilmRequest newFilmRequest) {
        List<Integer> directorIds = newFilmRequest.getDirectors().stream().map(Director::getId).toList();
        return directorService.getById(directorIds);
    }

    private List<FilmGenre> getFilmGenres(UpdateFilmRequest updateFilmRequest) {
        List<Integer> ids = updateFilmRequest.getGenres().stream().map(FilmGenreDto::getId).toList();
        return filmGenreStorage.getById(ids);
    }

    private List<Director> getFilmDirectors(UpdateFilmRequest updateFilmRequest) {
        List<Integer> directorIds = updateFilmRequest.getDirectors().stream().map(Director::getId).toList();
        return directorService.getById(directorIds);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        if (userId == null || friendId == null) {
            throw new BadRequestException("плохой запрос", "некорректные id пользователей");
        }
        userService.getUserById(userId);
        userService.getUserById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getPopularFilmsByYear(int count, int year) {
        return filmStorage.getPopularFilmsByYear(count, year);
    }

    public List<Film> getPopularFilmsByGenre(int count, int genre) {
        return filmStorage.getPopularFilmsByGenre(count, genre);
    }

    public List<Film> getPopularFilmsByYearAndGenre(int count, int year, int genreId) {
        return filmStorage.getPopularFilmsByYearGenre(count, year, genreId);
    }

    public Film getFilmById(long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("не найден фильм", "не найден фильм с id = " + id));
    }

    public List<Film> searchFilms(String queryString, String searchBy) {
        return filmStorage.searchBy(queryString, searchBy);
    }

    public Film updateFilm(@Valid UpdateFilmRequest updateFilmRequest) {

        Long filmId = updateFilmRequest.getId();

        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("не найден фильм", "не найден фильм с id = " + filmId));

        if (updateFilmRequest.getName() != null) {
            film.setName(updateFilmRequest.getName());
        }

        if (updateFilmRequest.getDescription() != null) {
            film.setDescription(updateFilmRequest.getDescription());
        }

        if (updateFilmRequest.getReleaseDate() != null) {
            film.setReleaseDate(updateFilmRequest.getReleaseDate());
        }

        if (updateFilmRequest.getDuration() != null) {
            film.setDuration(updateFilmRequest.getDuration());
        }

        if (updateFilmRequest.getMpa() != null) {
            film.setMpa(getFilmMpa(updateFilmRequest));
        }

        if (updateFilmRequest.getGenres() != null) {
            List<FilmGenre> genres = getFilmGenres(updateFilmRequest);
            if (genres.isEmpty()) {
                throw new BadRequestException("неуспешный запрос", "пустой список жанров");
            }
            film.setGenres(genres);
        }

        if (updateFilmRequest.getDirectors() != null) {
            List<Director> directors = getFilmDirectors(updateFilmRequest);
            directorService.validateDirectorsCreateAndUpdate(directors, updateFilmRequest.getDirectors().size());
            film.setDirectors(directors);
        }

        log.debug("updating film {}", film);
        return filmStorage.save(film);
    }

    public void deleteFilmById(long filmId) {

        log.info("удаляем фильм {}", filmId);

        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("не найден фильм", "не найден фильм по id = " + filmId));

        filmStorage.delete(film);
    }

    public int deleteAllFilms() {
        return filmStorage.deleteAll();
    }

    public List<Film> getSortedFilmsByDirector(int directorId, String sortBy) {
        Director director = directorService.getById(directorId);
        return filmStorage.getSortedFilmsByDirector(director, sortBy);
    }
}
