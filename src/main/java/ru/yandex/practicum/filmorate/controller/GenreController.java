package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping(value = {"", "/"})
    public List<FilmGenreDto> getAll() {
        return genreService.getAll().stream().map(FilmGenreMapper::mapToDto).toList();
    }

    @GetMapping("/{id}")
    public FilmGenreDto getFilmById(@PathVariable Integer id) {
        FilmGenre genre = genreService.getById(id);
        return FilmGenreMapper.mapToDto(genre);
    }
}
