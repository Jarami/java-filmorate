package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmMpa;

import java.util.*;

@Repository
public class InMemoryFilmMpaStorage implements FilmMpaStorage {

    private static int id;
    private final Map<Integer, String> mpas;

    public InMemoryFilmMpaStorage() {

        mpas = new HashMap<>();
        mpas.put(1, "G");
        mpas.put(2, "PG");
        mpas.put(3, "PG-13");
        mpas.put(4, "R");
        mpas.put(5, "NC-17");

        id = 6;
    }

    @Override
    public FilmMpa save(FilmMpa mpa) {
        if (mpa.getId() == null) {
            mpa.setId(generateId());
        }

        mpas.put(mpa.getId(), mpa.getName());
        return mpa;
    }

    @Override
    public List<FilmMpa> getAll() {
        List<FilmMpa> result = new ArrayList<>();
        mpas.forEach((id, name) -> result.add(new FilmMpa(id, name)));
        return result;
    }

    @Override
    public Optional<FilmMpa> getById(Integer id) {
        String name = mpas.get(id);
        if (name == null) {
            return Optional.empty();
        } else {
            return Optional.of(new FilmMpa(id, name));
        }
    }

    @Override
    public void delete(FilmMpa mpa) {
        mpas.remove(mpa.getId());
    }

    @Override
    public int deleteAll() {
        int filmCount = mpas.size();
        mpas.clear();
        return filmCount;
    }

    private int generateId() {
        return id++;
    }
}
