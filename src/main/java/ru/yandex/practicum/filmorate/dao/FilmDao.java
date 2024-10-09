package ru.yandex.practicum.filmorate.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Film;

@Repository
public class FilmDao implements EntityDao<Integer, Film> {

    private static int id = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    public void save(Film film) {
        if (film.getId() == null) {
            film.setId(generateId());
        }

        films.put(film.getId(), film);
    }

    public Collection<Film> getAll() {
        return films.values();
    }

    public Film getById(Integer id) {
        return films.get(id);
    }

    public void delete(Film film) {
        films.remove(film.getId());
    }

    private int generateId() {
        return id++;
    }
}
