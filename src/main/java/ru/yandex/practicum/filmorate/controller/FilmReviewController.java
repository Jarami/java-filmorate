package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewFilmReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmReviewRequest;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.service.FilmReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class FilmReviewController {

    private final FilmReviewService reviewService;

    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public FilmReview createFilmReview(@RequestBody NewFilmReviewRequest request) {
        return reviewService.createReview(request);
    }

    @PutMapping(value = {"", "/"})
    public FilmReview updateFilmReview(@RequestBody UpdateFilmReviewRequest request) {
        return reviewService.updateReview(request);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmReviewById(@PathVariable Long id) {
        reviewService.deleteById(id);
    }

    @GetMapping("/{id}")
    public FilmReview getFilmReviewById(@PathVariable Long id) {
        return reviewService.getById(id);
    }

    @GetMapping(value = {"", "/"})
    public List<FilmReview> getByFilmAndCount(@RequestParam(required = false, defaultValue = "10") Integer count,
                                              @RequestParam(required = false) Long filmId) {

        return reviewService.getByFilmAndCount(count, filmId);
    }

    @DeleteMapping(value = {"", "/"})
    public int deleteAllFilmReviews() {
        return reviewService.deleteAllReviews();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        reviewService.addLikeToReview(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        reviewService.addDislikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeToReview(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        reviewService.deleteLikeToReview(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeToReview(@PathVariable("id") Long reviewId, @PathVariable Long userId) {
        reviewService.deleteDislikeToReview(reviewId, userId);
    }
}
