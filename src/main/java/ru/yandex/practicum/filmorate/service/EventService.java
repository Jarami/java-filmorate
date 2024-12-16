package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;

    public List<Event> findEventsByUser(User user) {
        return eventStorage.findEventsByUserID(user.getId());
    }

    private Event createEvent(Long userID, EventType eventType, Operation operation, Long entityId) {
        Event event = EventMapper.mapToEvent(userID, eventType, operation, entityId, Instant.now());
        return eventStorage.createEvent(event);
    }

    public Event createAddLikeEvent(Long userID, Long filmId) {
        return createEvent(userID, EventType.LIKE, Operation.ADD, filmId);
    }

    public Event createRemoveLikeEvent(Long userID, Long filmId) {
        return createEvent(userID, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public Event createReviewEvent(Long userID, Operation operation, Long reviewId) {
        return createEvent(userID, EventType.REVIEW, operation, reviewId);
    }

    public Event createAddFriendEvent(Long userID, Long friendId) {
        return createEvent(userID, EventType.FRIEND, Operation.ADD, friendId);
    }

    public Event createRemoveFriendEvent(Long userID, Long friendId) {
        return createEvent(userID, EventType.FRIEND, Operation.REMOVE, friendId);
    }
}