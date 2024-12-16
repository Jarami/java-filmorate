package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmMpaDto;
import ru.yandex.practicum.filmorate.model.FilmMpa;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMpaMapper {

    public static FilmMpaDto mapToDto(FilmMpa mpa) {
        return FilmMpaDto.builder()
                .id(mpa.getId())
                .name(mpa.getName())
                .build();
    }

    public static FilmMpa mapToMpa(FilmMpaDto dto) {
        return FilmMpa.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
