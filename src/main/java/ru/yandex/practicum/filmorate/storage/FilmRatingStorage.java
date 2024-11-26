package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.List;
import java.util.Optional;

public interface FilmRatingStorage extends AbstractStorage<Integer, FilmRating> {

    List<FilmRating> getAll();

    Optional<FilmRating> getById(Integer filmId);

    FilmRating save(FilmRating rating);

    void delete(FilmRating rating);

    int deleteAll();
}
