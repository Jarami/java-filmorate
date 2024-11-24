package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.List;
import java.util.Optional;

public interface FilmGenreStorage extends AbstractStorage<Integer, FilmGenre> {
    public Optional<FilmGenre> getByName(String name);
    public List<FilmGenre> getById(List<Integer> ids);
}
