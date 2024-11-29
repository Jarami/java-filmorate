package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmGenreMapper {

    public static FilmGenreDto mapToDto(FilmGenre genre) {
        return new FilmGenreDto(genre.getId());
    }

    public static List<FilmGenreDto> mapToDto(List<FilmGenre> genres) {
        return genres.stream().map(FilmGenreMapper::mapToDto).toList();
    }

}
