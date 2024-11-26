package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import static ru.yandex.practicum.filmorate.model.FriendshipStatus.*;

@Getter
@Setter
@Builder
@ToString
public class Friendship {
    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;

    public boolean isAccepted() {
        return status == ACCEPTED;
    }

    public boolean isPending() {
        return status == PENDING;
    }
}
