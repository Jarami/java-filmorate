package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dto.NewFilmReviewRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmReviewRequest;
import ru.yandex.practicum.filmorate.exceptions.BadRequestException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmReviewStorage;

import java.util.List;

@Slf4j
@Service
@Validated
public class FilmReviewService {

    private final UserService userService;
    private final FilmService filmService;
    private final FilmReviewStorage reviewStorage;

    private final EventService eventService;

    public FilmReviewService(
            UserService userService,
            FilmService filmService,
            @Qualifier("db") FilmReviewStorage reviewStorage,
            EventService eventService) {

        this.userService = userService;
        this.filmService = filmService;
        this.reviewStorage = reviewStorage;
        this.eventService = eventService;
    }

    public FilmReview getById(long id) {
        return reviewStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("не найден отзыв", "не найден отзыв с id = " + id));
    }

    public FilmReview createReview(@Valid NewFilmReviewRequest request) {
        log.info("creating film review {}", request);

        User user = userService.getUserById(request.getUserId()); // проверка на существование
        Film film = filmService.getFilmById(request.getFilmId()); // проверка на существование

        FilmReview review = FilmReview.builder()
                .filmId(request.getFilmId())
                .userId(request.getUserId())
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .build();

        FilmReview r = reviewStorage.save(review);
        eventService.createReviewEvent(r.getUserId(), Operation.ADD, r.getReviewId());
        return r;
    }

    public FilmReview updateReview(@Valid UpdateFilmReviewRequest request) {

        log.info("updating film review {}", request);

        FilmReview review = reviewStorage.getById(request.getReviewId())
                .orElseThrow(() ->
                        new NotFoundException("не найден отзыв", "не найден отзыв с id = " + request.getReviewId()));

        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        if (request.getIsPositive() != null) {
            review.setPositive(request.getIsPositive());
        }

        FilmReview r = reviewStorage.save(review);
        eventService.createReviewEvent(review.getUserId(), Operation.UPDATE, review.getReviewId());
        return r;
    }

    public void deleteById(long id) {
        FilmReview review = reviewStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("не найден отзыв", "не найден отзыв с id = " + id));

        reviewStorage.delete(review);
        eventService.createReviewEvent(review.getUserId(), Operation.REMOVE, review.getReviewId());
    }

    public List<FilmReview> getByFilmAndCount(Integer count, Long filmId) {

        if (count < 0) {
            throw new BadRequestException("плохой запрос", "count не может быть отрицательным");
        }

        if (filmId != null) {
            Film film = filmService.getFilmById(filmId);
            return reviewStorage.getAll(count, film);
        }

        return reviewStorage.getAll(count);
    }

    public int deleteAllReviews() {
        return reviewStorage.deleteAll();
    }

    public void addLikeToReview(long reviewId, long userId) {
        FilmReview review = getById(reviewId);
        User user = userService.getUserById(userId);

        reviewStorage.addLikeToReview(review, user);
    }

    public void addDislikeToReview(long reviewId, long userId) {
        FilmReview review = getById(reviewId);
        User user = userService.getUserById(userId);

        reviewStorage.addDislikeToReview(review, user);
    }

    public void deleteLikeToReview(long reviewId, long userId) {
        FilmReview review = getById(reviewId);
        User user = userService.getUserById(userId);

        reviewStorage.deleteLikeToReview(review, user);
    }

    public void deleteDislikeToReview(long reviewId, long userId) {
        FilmReview review = getById(reviewId);
        User user = userService.getUserById(userId);

        reviewStorage.deleteDislikeToReview(review, user);
    }
}
