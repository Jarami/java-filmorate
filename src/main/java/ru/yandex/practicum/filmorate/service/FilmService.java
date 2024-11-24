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
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.FilmRatingStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@Validated
public class FilmService {

    private final FilmStorage filmStorage;
    private final FilmRatingStorage filmRatingStorage;
    private final FilmGenreStorage filmGenreStorage;

    public FilmService(
            @Qualifier("db") FilmStorage filmStorage,
            @Qualifier("db") FilmRatingStorage filmRatingStorage,
            @Qualifier("db") FilmGenreStorage filmGenreStorage) {

        this.filmStorage = filmStorage;
        this.filmRatingStorage = filmRatingStorage;
        this.filmGenreStorage = filmGenreStorage;
    }

    public Film createFilm(@Valid NewFilmRequest newFilmRequest) {

        log.info("creating film {}", newFilmRequest);

        Film film = FilmMapper.mapToFilm(newFilmRequest);
        film.setRating(getFilmRating(newFilmRequest));

        List<FilmGenre> genres = getFilmGenres(newFilmRequest);
        if (genres.isEmpty()) {
            throw new BadRequestException("неуспешный запрос", "пустой список жанров");
        }
        film.setGenres(genres);

        log.info("saving film {}", film);
        return filmStorage.save(film);
    }

    private FilmRating getFilmRating(NewFilmRequest newFilmRequest) {
        Integer ratingId = newFilmRequest.getRating().getId();

        return filmRatingStorage.getById(ratingId)
                .orElseThrow(() ->
                        new NotFoundException("не найден рейтинг", "не найден рейтинг по id " + ratingId));
    }

    private FilmRating getFilmRating(UpdateFilmRequest updateFilmRequest) {
        Integer ratingId = updateFilmRequest.getRating().getId();

        return filmRatingStorage.getById(ratingId)
                .orElseThrow(() ->
                        new NotFoundException("не найден рейтинг", "не найден рейтинг по id " + ratingId));
    }

    private List<FilmGenre> getFilmGenres(NewFilmRequest newFilmRequest) {
        List<Integer> ids = newFilmRequest.getGenres().stream().map(FilmGenreDto::getId).toList();
        return filmGenreStorage.getById(ids);
    }

    private List<FilmGenre> getFilmGenres(UpdateFilmRequest updateFilmRequest) {
        List<Integer> ids = updateFilmRequest.getGenres().stream().map(FilmGenreDto::getId).toList();
        return filmGenreStorage.getById(ids);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(long id) {
        checkFilmId(id);
        return filmStorage.getById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
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

        if (updateFilmRequest.getRating() != null) {
            film.setRating(getFilmRating(updateFilmRequest));
        }

        if (updateFilmRequest.getGenres() != null) {
            List<FilmGenre> genres = getFilmGenres(updateFilmRequest);
            if (genres.isEmpty()) {
                throw new BadRequestException("неуспешный запрос", "пустой список жанров");
            }
            film.setGenres(genres);
        }

        log.info("saving film {}", film);
        return filmStorage.save(film);
    }

    public void deleteFilmById(long filmId) {
        checkFilmId(filmId);
        filmStorage.getById(filmId)
            .ifPresent(filmStorage::delete);
    }

    public int deleteAllFilms() {
        return filmStorage.deleteAll();
    }

    private void checkFilmId(Long filmId) {
        if (filmId == null) {
            throw new FilmNotFoundException(null);
        }
    }
}
