package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.mapper.EventRowMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DbEventStorage extends NamedRepository<Event> implements EventStorage {
    private static final String INSERT_QUERY = """
        INSERT INTO films(film_name, description, release_date, duration, mpa_id)
        VALUES (:name, :description, :releaseDate, :duration, :mpaId)""";  // @TODO: make for event

    private static final String UPDATE_QUERY = """
        UPDATE films
        SET film_name = :name,
            description = :description,
            release_date = :releaseDate,
            duration = :duration,
            mpa_id = :mpaId
        WHERE film_id = :filmId""";  // @TODO: make for event

    private static final String FIND_EVENTS_BY_USER_ID_QUERY = "SELECT * FROM EVENTS WHERE USER_ID = :userId";
    @Autowired
    public DbEventStorage(NamedParameterJdbcTemplate namedTemplate, EventRowMapper mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public Event createEvent(Event event) {
        if (event.getEventId() == null) {
            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of(
                            "EVENT_TIMESTAMP", event.getTimestamp(),
                            "USER_ID", event.getUserId(),
                            "EVENT_TYPE", event.getEventType().getTitle(),
                            "OPERATION",event.getOperation().getTitle(),
                            "ENTITY_ID", event.getEntityId()),
                    new String[]{"EVENT_ID"}
            );
            Long id = keyHolder.getKeyAs(Long.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать событие " + event);
            } else {
                event.setId(id);
                log.debug("Событие типа {} сохранено с id = {}", event.getClass(), event.getId());
            }
        } else {
            update(
                    UPDATE_QUERY,
                    Map.of(
                            "EVENT_TIMESTAMP", event.getTimestamp(),
                            "USER_ID", event.getUserId(),
                            "EVENT_TYPE", event.getEventType().getTitle(),
                            "OPERATION",event.getOperation().getTitle(),
                            "ENTITY_ID", event.getEntityId())
            );
        }

//        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
//                .withTableName("EVENTS")
//                .usingGeneratedKeyColumns("EVENT_ID");
//
//        Map<String, Object> values = new HashMap<>();
//        values.put("EVENT_TIMESTAMP", event.getTimestamp());
//        values.put("USER_ID", event.getUserId());
//        String eventType = event.getEventType().getTitle();
//        values.put("EVENT_TYPE", eventType);
//        String operation = event.getOperation().getTitle();
//        values.put("OPERATION", operation);
//        values.put("ENTITY_ID", event.getEntityId());
//
//        event.setEventId(simpleJdbcInsert.executeAndReturnKey(values).longValue());
//        return event;

        return event;
    }

//    @Override
//    public Film save(Film film) {
//        if (film.getId() == null) {
//            KeyHolder keyHolder = insert(
//                    INSERT_QUERY,
//                    Map.of(
//                            "name", film.getName(),
//                            "description", film.getDescription(),
//                            "releaseDate", film.getReleaseDate(),
//                            "duration", film.getDuration(),
//                            "mpaId", film.getMpa().getId()),
//                    new String[]{"film_id"}
//            );
//            Long id = keyHolder.getKeyAs(Long.class);
//            if (id == null) {
//                throw new FailedToCreateEntity("не удалось создать фильм " + film);
//            } else {
//                film.setId(id);
//                log.debug("Фильм {} сохранен с id = {}", film.getName(), film.getId());
//            }
//
//        } else {
//
//            update(
//                    UPDATE_QUERY,
//                    Map.of(
//                            "name", film.getName(),
//                            "description", film.getDescription(),
//                            "releaseDate", film.getReleaseDate(),
//                            "duration", film.getDuration(),
//                            "mpaId", film.getMpa().getId(),
//                            "filmId", film.getId())
//            );
//        }
//
//        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
//            saveGenres(film);
//        }
//
//        return film;
//    }

    @Override
    public List<Event> findEventsByUserID(Long id) {
        return findMany(FIND_EVENTS_BY_USER_ID_QUERY,
                Map.of("userId", id), new BeanPropertyRowMapper<>(Event.class));
    }
}