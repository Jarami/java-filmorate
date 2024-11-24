package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmRating;
import ru.yandex.practicum.filmorate.storage.FilmRatingStorage;

import java.util.Collection;

@Service
public class MpaService {

    private final FilmRatingStorage filmRatingStorage;

    public MpaService(@Qualifier("db") FilmRatingStorage filmRatingStorage) {
        this.filmRatingStorage = filmRatingStorage;
    }

    public Collection<FilmRating> getAll() {
        return filmRatingStorage.getAll();
    }

    public FilmRating getById(int id) {
        return filmRatingStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("не найден рейтинг", "не найден рейтинг по id " + id));
    }
}
