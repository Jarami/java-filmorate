package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

public class FilmMapper {
    private FilmMapper() {

    }

    public static Film fromDto(FilmDto filmDto) {
        return new Film(
                filmDto.getId(),
                filmDto.getName(),
                filmDto.getDescription(),
                filmDto.getReleaseDate(),
                filmDto.getDuration()
        );
    }

    public static FilmDto toDto(Film film) {
        return new FilmDto(
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
    }
}
