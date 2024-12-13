package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

@Getter
@Setter
@ToString
@Builder
public class EventDto {
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Long eventId;
}
