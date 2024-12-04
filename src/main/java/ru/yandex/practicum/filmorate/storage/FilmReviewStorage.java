package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmReview;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface FilmReviewStorage {
    FilmReview save(FilmReview filmReview);
    void delete(FilmReview filmReview);
    int deleteAll();
    Optional<FilmReview> getById(long id);
    List<FilmReview> getAll(int count, Film film);
    List<FilmReview> getAll(int count);
    void addLikeToReview(FilmReview filmReview, User user);
    void addDislikeToReview(FilmReview filmReview, User user);
    void deleteLikeToReview(FilmReview filmReview, User user);
    void deleteDislikeToReview(FilmReview filmReview, User user);
}
