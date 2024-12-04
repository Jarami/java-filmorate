package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review save(Review review);
    void delete(Review review);
    int deleteAll();
    Optional<Review> getById(long id);
    List<Review> getAll(int count, Film film);
    List<Review> getAll(int count);
    void addLikeToReview(Review review, User user);
    void addDislikeToReview(Review review, User user);
    void deleteLikeToReview(Review review, User user);
    void deleteDislikeToReview(Review review, User user);
}
