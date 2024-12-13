package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.ResponseDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.LikeService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count,
                                         @RequestParam(required = false) Integer genreId,
                                         @RequestParam(required = false) Integer year) {

        List<Film> films;
        if (genreId == null && year == null) {
            films = likeService.getPopularFilms(count);
        } else if (genreId == null) {
            films = likeService.getPopularFilmsByYear(count, year);
        } else if (year == null) {
            films = likeService.getPopularFilmsByGenre(count, genreId);
        } else {
            films = likeService.getPopularFilmsByYearAndGenre(count, year, genreId);
        }
        return FilmMapper.mapToDto(films);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseDto like(@PathVariable Long filmId, @PathVariable Long userId) {
        boolean result = likeService.like(filmId, userId);
        return new ResponseDto(result);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseDto dislike(@PathVariable Long filmId, @PathVariable Long userId) {
        boolean result = likeService.dislike(filmId, userId);
        return new ResponseDto(result);
    }
}
