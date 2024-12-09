package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@Builder
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private FilmMpa mpa;
    private List<FilmGenre> genres;
    private List<Director> directors;
    private int rate;

    public Film() {
        genres = new ArrayList<>();
        directors = new ArrayList<>();
    }

    public Film(Long id, String name, String description, String mpaName, LocalDate releaseDate, int duration) {
        this(id, name, description, releaseDate, duration, null, new ArrayList<>(), new ArrayList<>(), 0);
    }
}
