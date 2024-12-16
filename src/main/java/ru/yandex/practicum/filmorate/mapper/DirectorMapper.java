package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DirectorMapper {

    public static Director mapToDirector(DirectorDto dto) {
        return Director.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    public static List<Director> mapToDirector(List<DirectorDto> dtos) {
        return dtos.stream().map(DirectorMapper::mapToDirector).toList();
    }

    public static DirectorDto mapToDto(Director director) {
        return DirectorDto.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    public static List<DirectorDto> mapToDto(List<Director> directors) {
        return directors.stream().map(DirectorMapper::mapToDto).toList();
    }
}
