package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {
    @Override
    public Friendship mapRow(ResultSet rs, int rowNum) throws SQLException {

        Timestamp acceptedAt = rs.getTimestamp("accepted_at");

        return Friendship.builder()
                .userId(rs.getLong("user_id"))
                .friendId(rs.getLong("friend_id"))
                .status(FriendshipStatus.valueOf(rs.getString("status").toUpperCase()))
                .requestedAt(rs.getTimestamp("requested_at").toLocalDateTime())
                .acceptedAt(acceptedAt == null ? null : acceptedAt.toLocalDateTime())
                .build();
    }
}
