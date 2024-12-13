package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class FilmDto {
    private long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private FilmMpaDto mpa;
    private List<FilmGenreDto> genres;
    private List<DirectorDto> directors;
    private int rate;
}
