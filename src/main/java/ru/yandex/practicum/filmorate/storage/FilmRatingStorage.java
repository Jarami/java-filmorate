package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.Optional;

public interface FilmRatingStorage extends AbstractStorage<Integer, FilmRating> {
    public Optional<FilmRating> getByName(String name);
}
