package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Qualifier("db")
public class DbEventStorage extends NamedRepository<Event> implements EventStorage {
    private static final String INSERT_QUERY = """
        INSERT INTO events (event_timestamp, user_id, event, operation, entity_id)
        VALUES (:event_timestamp, :user_id, :event, :operation, :entity_id)""";

    private static final String FIND_EVENTS_BY_USER_ID_QUERY = "SELECT * FROM EVENTS WHERE USER_ID = :userId";

    @Autowired
    public DbEventStorage(NamedParameterJdbcTemplate namedTemplate, EventRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public Event createEvent(Event event) {
        if (event.getId() == null) {
            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of(
                            "event_timestamp", Timestamp.from(Instant.ofEpochMilli(event.getTimestamp())),
                            "user_id", event.getUserId(),
                            "event", event.getEventType().getTitle(),
                            "operation",event.getOperation().getTitle(),
                            "entity_id", event.getEntityId()),
                    new String[]{"event_id"}
            );
            Long id = keyHolder.getKeyAs(Long.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать событие " + event);
            } else {
                event.setId(id);
                log.debug("Событие типа {} сохранено с id = {}", event.getClass(), event.getId());
            }
        }

        return event;
    }

    @Override
    public List<Event> findEventsByUserID(Long id) {
        return findMany(FIND_EVENTS_BY_USER_ID_QUERY, Map.of("userId", id));
    }
}