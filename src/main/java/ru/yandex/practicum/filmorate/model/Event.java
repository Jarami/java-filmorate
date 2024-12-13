package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;

@Getter
@Setter
@Validated
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@Builder
public class Event {
    private Instant timestamp;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Long id;
}