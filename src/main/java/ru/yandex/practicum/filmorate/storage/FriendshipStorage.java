package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendshipStorage {

    List<Long> getFriends(User user);

    List<Long> getCommonFriends(User user1, User user2);

    boolean addFriend(User user, User friend);

    boolean removeFriend(User user, User friend);
}
