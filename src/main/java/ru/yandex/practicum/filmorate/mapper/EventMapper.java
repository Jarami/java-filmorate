package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.time.Instant;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event mapToEvent(Long userID, EventType eventType, Operation operation, Long entityId, Instant instant) {
        return Event.builder()
                .timestamp(instant.toEpochMilli())
                .eventType(eventType)
                .operation(operation)
                .userId(userID)
                .entityId(entityId)
                .build();
    }
}
