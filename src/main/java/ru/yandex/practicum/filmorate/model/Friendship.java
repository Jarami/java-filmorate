package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class Friendship {
    private Long id;
    private Long sendingUserId;
    private Long receivingUserId;
    private FriendshipStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime acceptedAt;

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    public boolean isPending() {
        return status == FriendshipStatus.PENDING;
    }
}
