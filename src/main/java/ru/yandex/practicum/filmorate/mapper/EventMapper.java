package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.time.Instant;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    public static Event mapToEvent(Long userID, EventType eventType, Operation operation, Long entityId, Instant instant) {
        return Event.builder()
                .timestamp(instant)
                .eventType(eventType)
                .operation(operation)
                .userId(userID)
                .entityId(entityId)
                .build();
    }

    public static EventDto mapToDto(Event event) {
        return EventDto.builder()
                .eventId(event.getId())
                .timestamp(event.getTimestamp().toEpochMilli())
                .eventType(event.getEventType())
                .operation(event.getOperation())
                .userId(event.getUserId())
                .entityId(event.getEntityId())
                .build();
    }
}
