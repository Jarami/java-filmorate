package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Film.
 */
@Data
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film {
    @EqualsAndHashCode.Include
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
}
