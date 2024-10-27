package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFound;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryFilmStorage implements FilmStorage {

    private static int id = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film save(Film film) {
        if (film.getId() == null) {
            film.setId(generateId());
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new FilmNotFound(film);
        }

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(Film film) {
        if (film.getId() == null) {
            throw new FilmNotFound(film);
        }

        films.remove(film.getId());
    }

    private int generateId() {
        return id++;
    }
}
