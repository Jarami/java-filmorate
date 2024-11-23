package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmRating;

import java.util.*;

@Repository
public class InMemoryFilmRatingStorage implements FilmRatingStorage {

    private static int id;
    private final Map<Integer, String> ratings;

    public InMemoryFilmRatingStorage() {
        
        ratings = new HashMap<>();
        ratings.put(1, "G");
        ratings.put(2, "PG");
        ratings.put(3, "PG-13");
        ratings.put(4, "R");
        ratings.put(5, "NC-17");
        
        id = 6;
    }

    @Override
    public FilmRating save(FilmRating rating) {
        if (rating.getId() == null) {
            rating.setId(generateId());
        }

        ratings.put(rating.getId(), rating.getName());
        return rating;
    }

    @Override
    public Collection<FilmRating> getAll() {
        List<FilmRating> result = new ArrayList<>();
        ratings.forEach( (id, name) -> result.add(new FilmRating(id, name)));
        return result;
    }

    @Override
    public Optional<FilmRating> getById(Integer id) {
        String name = ratings.get(id);
        if (name == null) {
            return Optional.empty();
        } else {
            return Optional.of(new FilmRating(id, name));
        }
    }

    @Override
    public Optional<FilmRating> getByName(String name) {

        for (Map.Entry<Integer, String> entry : ratings.entrySet()) {
            if (entry.getValue().equals(name)) {
                return Optional.of(new FilmRating(entry.getKey(), entry.getValue()));
            }
        }

        return Optional.empty();
    }

    @Override
    public void delete(FilmRating rating) {
        ratings.remove(rating.getId());
    }

    @Override
    public int deleteAll() {
        int filmCount = ratings.size();
        ratings.clear();
        return filmCount;
    }

    private int generateId() {
        return id++;
    }
}
