package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaResponse;
import ru.yandex.practicum.filmorate.mapper.FilmRatingMapper;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping(value = {"", "/"})
    public List<MpaResponse> getAll() {
        return mpaService.getAll().stream()
                .map(FilmRatingMapper::mapToResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public MpaResponse getFilmById(@PathVariable int id) {
        return FilmRatingMapper.mapToResponse(mpaService.getById(id));
    }
}
