package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Event.builder()
                .id(resultSet.getLong("event_id"))
                .timestamp(resultSet.getTimestamp("event_timestamp").toInstant())
                .eventType(EventType.valueOf(resultSet.getString("event")))
                .operation(Operation.valueOf(resultSet.getString("operation")))
                .userId(resultSet.getLong("user_id"))
                .entityId(resultSet.getLong("entity_id"))
                .build();
    }
}
