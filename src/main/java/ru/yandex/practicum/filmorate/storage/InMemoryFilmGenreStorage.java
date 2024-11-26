package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.*;

public class InMemoryFilmGenreStorage implements FilmGenreStorage {

    private static int id;
    private final Map<Integer, String> genres;

    public InMemoryFilmGenreStorage() {

        genres = new HashMap<>();
        genres.put(1, "G");
        genres.put(2, "PG");
        genres.put(3, "PG-13");
        genres.put(4, "R");
        genres.put(5, "NC-17");

        id = 6;
    }

    @Override
    public FilmGenre save(FilmGenre genre) {
        if (genre.getId() == null) {
            genre.setId(generateId());
        }

        genres.put(genre.getId(), genre.getName());
        return genre;
    }

    @Override
    public List<FilmGenre> getAll() {
        List<FilmGenre> result = new ArrayList<>();
        genres.forEach((id, name) -> result.add(new FilmGenre(id, name)));
        return result;
    }

    @Override
    public Optional<FilmGenre> getById(Integer id) {
        String name = genres.get(id);
        if (name == null) {
            return Optional.empty();
        } else {
            return Optional.of(new FilmGenre(id, name));
        }
    }

    @Override
    public List<FilmGenre> getById(List<Integer> ids) {
        return ids.stream()
                .map(id -> new FilmGenre(id, genres.get(id)))
                .toList();
    }

    @Override
    public void delete(FilmGenre genre) {
        genres.remove(genre.getId());
    }

    @Override
    public int deleteAll() {
        int filmCount = genres.size();
        genres.clear();
        return filmCount;
    }

    private int generateId() {
        return id++;
    }
}
