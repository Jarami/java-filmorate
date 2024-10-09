package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class FilmDto {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
}
