package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review save(Review review);
    void delete(Review review);
    int deleteAll();
    Optional<Review> getById(long id);
    List<Review> getAll(int count, Film film);
    List<Review> getAll(int count);
    void addLikeToReview(long reviewId, long userId);
    void addDislikeToReview(long reviewId, long userId);
    void deleteLikeToReview(long reviewId, long userId);
    void deleteDislikeToReview(long reviewId, long userId);
}
