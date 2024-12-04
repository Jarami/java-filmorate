package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mapper.ReviewRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("db")
public class DbReviewStorage extends NamedRepository<Review> implements ReviewStorage {

    private static final String INSERT_QUERY = """
        INSERT INTO reviews(film_id, user_id, content, is_positive)
        VALUES (:filmId, :userId, :content, :isPositive)""";

    private static final String UPDATE_QUERY = """
        UPDATE reviews
        SET film_id = :filmId,
            user_id = :userId,
            content = :content,
            is_positive = :isPositive
        WHERE review_id = :reviewId""";

    private static final String FIND_BY_ID_QUERY = """
        SELECT r.review_id as "review_id",
               r.film_id as "film_id",
               r.user_id as "user_id",
               r.content as "content",
               r.is_positive as "is_positive",
               0 as "rate"
        FROM reviews r
        WHERE r.review_id = :reviewId""";

    private static final String FIND_BY_FILM_ID_WITH_LIMIT_QUERY = """
        SELECT r.review_id as "review_id",
               r.film_id as "film_id",
               r.user_id as "user_id",
               r.content as "content",
               r.is_positive as "is_positive",
               0 as "rate"
        FROM reviews r
        WHERE r.film_id = :filmId
        LIMIT :count""";

    private static final String FIND_WITH_LIMIT_QUERY = """
        SELECT r.review_id as "review_id",
               r.film_id as "film_id",
               r.user_id as "user_id",
               r.content as "content",
               r.is_positive as "is_positive",
               0 as "rate"
        FROM reviews r
        LIMIT :count""";

    private static final String DELETE_QUERY = """
        DELETE FROM reviews
        WHERE review_id = :reviewId""";

    public DbReviewStorage(NamedParameterJdbcTemplate namedTemplate, ReviewRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public Review save(Review review) {
        if (review.getId() == null) {

            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of(
                        "filmId", review.getFilmId(),
                        "userId", review.getUserId(),
                        "content", review.getContent(),
                        "isPositive", review.isPositive()),
                    new String[]{"review_id"}
            );
            Long id = keyHolder.getKeyAs(Long.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать отзыв " + review);
            } else {
                review.setId(id);
                log.debug("Отзыв на фильм {} от пользователя {} сохранен с id = {}", review.getFilmId(),
                        review.getUserId(), review.getId());
            }

        } else {

            update(
                    UPDATE_QUERY,
                    Map.of(
                        "filmId", review.getFilmId(),
                        "userId", review.getUserId(),
                        "content", review.getContent(),
                        "isPositive", review.isPositive(),
                        "reviewId", review.getId())
            );
        }

        return review;
    }

    @Override
    public void delete(Review review) {
        delete(DELETE_QUERY, Map.of("reviewId", review.getId()));
    }

    @Override
    public int deleteAll() {
        return 0;
    }

    @Override
    public Optional<Review> getById(long id) {
        return findOne(FIND_BY_ID_QUERY, Map.of("reviewId", id));
    }

    @Override
    public List<Review> getAll(int count, Film film) {
        return findMany(FIND_BY_FILM_ID_WITH_LIMIT_QUERY, Map.of("filmId", film.getId(), "count", count));
    }

    @Override
    public List<Review> getAll(int count) {
        return findMany(FIND_WITH_LIMIT_QUERY, Map.of("count", count));
    }

    @Override
    public void addLikeToReview(long reviewId, long userId) {

    }

    @Override
    public void addDislikeToReview(long reviewId, long userId) {

    }

    @Override
    public void deleteLikeToReview(long reviewId, long userId) {

    }

    @Override
    public void deleteDislikeToReview(long reviewId, long userId) {

    }
}
