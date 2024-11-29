package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;
import java.util.Optional;

public interface FilmGenreStorage extends AbstractStorage<Integer, FilmGenre> {
    List<FilmGenre> getAll();

    Optional<FilmGenre> getById(Integer genreId);

    List<FilmGenre> getById(List<Integer> ids);

    FilmGenre save(FilmGenre mpa);

    void delete(FilmGenre mpa);

    int deleteAll();
}
