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
                .id(resultSet.getLong("EVENT_ID"))
                .timestamp(resultSet.getLong("EVENT_TIMESTAMP"))
                .eventType(EventType.valueOf(resultSet.getString("EVENT_TYPE")))
                .operation( Operation.valueOf(resultSet.getString("OPERATION")))
                .userId(resultSet.getLong("USER_ID"))
                .entityId(resultSet.getLong("ENTITY_ID"))
                .build();
    }
}
