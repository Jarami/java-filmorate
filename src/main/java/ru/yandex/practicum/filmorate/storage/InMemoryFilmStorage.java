package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryFilmStorage implements FilmStorage {

    private static long id = 1;
    private final Map<Long, Film> films = new HashMap<>();

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
    public Film getById(long id) {
        return films.get(id);
    }

    @Override
    public void delete(Film film) {
        films.remove(film.getId());
    }

    @Override
    public int deleteAll() {
        int filmCount = films.size();
        films.clear();
        return filmCount;
    }

    private long generateId() {
        return id++;
    }
}
