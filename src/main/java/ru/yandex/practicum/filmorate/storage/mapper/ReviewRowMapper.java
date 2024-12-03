package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getLong("review_id"))
                .filmId(rs.getLong("film_id"))
                .userId(rs.getLong("user_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .rate(rs.getInt("rate"))
                .build();
    }
}
