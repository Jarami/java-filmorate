package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;

import java.util.Collection;

@Service
public class GenreService {

    private final FilmGenreStorage filmGenreStorage;

    public GenreService(@Qualifier("db") FilmGenreStorage filmGenreStorage) {
        this.filmGenreStorage = filmGenreStorage;
    }

    public Collection<FilmGenre> getAll() {
        return filmGenreStorage.getAll();
    }

    public FilmGenre getById(int id) {
        return filmGenreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("не найден жанр", "не найден жанр по id " + id));
    }
}
