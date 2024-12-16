package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmMpaDto;
import ru.yandex.practicum.filmorate.mapper.FilmMpaMapper;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping(value = {"", "/"})
    public List<FilmMpaDto> getAll() {
        return mpaService.getAll().stream()
                .map(FilmMpaMapper::mapToDto)
                .toList();
    }

    @GetMapping("/{id}")
    public FilmMpaDto getFilmById(@PathVariable Integer id) {
        return FilmMpaMapper.mapToDto(mpaService.getById(id));
    }
}
