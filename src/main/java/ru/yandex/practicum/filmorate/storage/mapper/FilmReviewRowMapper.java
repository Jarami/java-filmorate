package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmReview;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmReviewRowMapper implements RowMapper<FilmReview> {

    @Override
    public FilmReview mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FilmReview.builder()
                .reviewId(rs.getLong("review_id"))
                .filmId(rs.getLong("film_id"))
                .userId(rs.getLong("user_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .rate(rs.getInt("rate"))
                .build();
    }
}
