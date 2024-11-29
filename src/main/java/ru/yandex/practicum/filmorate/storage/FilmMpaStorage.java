package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmMpa;

import java.util.List;
import java.util.Optional;

public interface FilmMpaStorage extends AbstractStorage<Integer, FilmMpa> {

    List<FilmMpa> getAll();

    Optional<FilmMpa> getById(Integer filmId);

    FilmMpa save(FilmMpa mpa);

    void delete(FilmMpa mpa);

    int deleteAll();
}
