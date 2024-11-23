package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonValue;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

public enum FilmRating {
    G ("G"),
    PG ("PG"),
    PG13 ("PG-13"),
    R ("R"),
    NC17 ("NC-17");

    private String name;

    FilmRating(String name) {
        this.name = name;
    }

    public static FilmRating getByName(String name) {
        FilmRating[] ratings = FilmRating.values();
        for (FilmRating rating : ratings) {
            if (rating.getName().equals(name)) {
                return rating;
            }
        }
        throw new NotFoundException("не найден рейтинг фильма", "не найден рейтинг фильма по значению " + name);
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
