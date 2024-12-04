package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
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
               SUM(rr.rate) as "rate"
        FROM reviews r
        LEFT JOIN review_rates rr ON rr.review_id = r.review_id
        WHERE r.review_id = :reviewId
        GROUP BY r.review_id
        ORDER BY SUM(rr.rate) DESC""";

    private static final String FIND_BY_FILM_ID_WITH_LIMIT_QUERY = """
        SELECT r.review_id as "review_id",
               r.film_id as "film_id",
               r.user_id as "user_id",
               r.content as "content",
               r.is_positive as "is_positive",
               SUM(rr.rate) as "rate"
        FROM reviews r
        LEFT JOIN review_rates rr ON rr.review_id = r.review_id
        WHERE r.film_id = :filmId
        GROUP BY r.review_id
        ORDER BY SUM(rr.rate) DESC
        LIMIT :count""";

    private static final String FIND_WITH_LIMIT_QUERY = """
        SELECT r.review_id as "review_id",
               r.film_id as "film_id",
               r.user_id as "user_id",
               r.content as "content",
               r.is_positive as "is_positive",
               SUM(rr.rate) as "rate"
        FROM reviews r
        LEFT JOIN review_rates rr ON rr.review_id = r.review_id
        GROUP BY r.review_id
        ORDER BY SUM(rr.rate) DESC
        LIMIT :count""";

    private static final String DELETE_QUERY = """
        DELETE FROM reviews
        WHERE review_id = :reviewId""";

    private static final String DELETE_ALL_QUERY = """
        DELETE FROM reviews""";

    private static final String INSERT_RATE_QUERY = """
        INSERT INTO review_rates(review_id, user_id, rate)
        VALUES (:reviewId, :userId, :rate)""";

    private static final String UPDATE_RATE_QUERY = """
        UPDATE review_rates
        SET rate = :rate
        WHERE review_id = :reviewId AND user_id = :userId""";

    private static final String COUNT_RATE_QUERY = """
        SELECT COUNT(*)
        FROM review_rates
        WHERE review_id = :reviewId AND user_id = :userId""";

    private static final String DELETE_RATE_QUERY = """
        DELETE FROM review_rates
        WHERE review_id = :reviewId AND user_id = :userId""";

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
        return delete(DELETE_ALL_QUERY);
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
    public void addLikeToReview(Review review, User user) {
        upsertRateToReview(review, user, 1);
    }

    @Override
    public void addDislikeToReview(Review review, User user) {
        upsertRateToReview(review, user, -1);
    }

    @Override
    public void deleteLikeToReview(Review review, User user) {
        delete(DELETE_RATE_QUERY, Map.of("reviewId", review.getId(), "userId", user.getId()));
    }

    @Override
    public void deleteDislikeToReview(Review review, User user) {
        delete(DELETE_RATE_QUERY, Map.of("reviewId", review.getId(), "userId", user.getId()));
    }

    private void upsertRateToReview(Review review, User user, int rate) {

        Integer count = queryForObject(COUNT_RATE_QUERY,
                Map.of("reviewId", review.getId(), "userId", user.getId()), Integer.class);

        if (count == 0) {
            insert(
                    INSERT_RATE_QUERY,
                    Map.of(
                            "reviewId", review.getId(),
                            "userId", user.getId(),
                            "rate", rate),
                    new String[]{"review_rate_id"}
            );
        } else {
            update(
                    UPDATE_RATE_QUERY,
                    Map.of(
                            "reviewId", review.getId(),
                            "userId", user.getId(),
                            "rate", rate)
            );
        }
    }
}
