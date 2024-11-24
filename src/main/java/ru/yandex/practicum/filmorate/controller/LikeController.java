package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.LikeService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count) {
        return likeService.getPopularFilms(count);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public FilmDto like(@PathVariable long filmId, @PathVariable long userId) {
        return likeService.like(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public FilmDto dislike(@PathVariable long filmId, @PathVariable long userId) {
        return likeService.dislike(filmId, userId);
    }
}
