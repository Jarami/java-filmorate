package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.storage.FilmMpaStorage;

import java.util.List;

@Service
public class MpaService {

    private final FilmMpaStorage filmMpaStorage;

    public MpaService(@Qualifier("db") FilmMpaStorage filmMpaStorage) {
        this.filmMpaStorage = filmMpaStorage;
    }

    public List<FilmMpa> getAll() {
        return filmMpaStorage.getAll();
    }

    public FilmMpa getById(int id) {
        return filmMpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("не найден рейтинг", "не найден рейтинг по id " + id));
    }
}
