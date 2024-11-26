package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendshipStorage {
    Collection<Long> getFriends(User user);
    Collection<Long> getCommonFriends(User user1, User user2);
    boolean addFriend(User user, User friend);
    boolean removeFriend(User user, User friend);
}
