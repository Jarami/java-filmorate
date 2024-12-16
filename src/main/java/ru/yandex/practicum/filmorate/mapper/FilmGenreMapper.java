package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmGenreMapper {

    public static FilmGenreDto mapToDto(FilmGenre genre) {
        return FilmGenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public static List<FilmGenreDto> mapToDto(List<FilmGenre> genres) {
        return genres.stream()
                .map(FilmGenreMapper::mapToDto)
                .toList();
    }

    public static FilmGenre mapToGenre(FilmGenreDto dto) {
        return FilmGenre.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    public static List<FilmGenre> mapToGenre(List<FilmGenreDto> dtos) {
        return dtos.stream().map(FilmGenreMapper::mapToGenre).toList();
    }
}
