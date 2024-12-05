package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ResponseDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.LikeService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count,
                                      @RequestParam(required = false) Integer genreId,
                                      @RequestParam(required = false) Integer year) {
        if (genreId == null && year == null) {
            return likeService.getPopularFilms(count);
        }
        if (genreId == null) {
            return likeService.getPopularFilmsByYear(count, year);
        }
        if (year == null) {
            return likeService.getPopularFilmsByGenre(count, genreId);
        }
        return likeService.getPopularFilmsByYearGenre(count, year, genreId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseDto like(@PathVariable long filmId, @PathVariable long userId) {
        boolean result = likeService.like(filmId, userId);
        return new ResponseDto(result);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseDto dislike(@PathVariable long filmId, @PathVariable long userId) {
        boolean result = likeService.dislike(filmId, userId);
        return new ResponseDto(result);
    }
}
