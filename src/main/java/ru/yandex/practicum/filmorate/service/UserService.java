package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("db") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Validated(Marker.OnCreate.class)
    public User createUser(@Valid User user) {
        setNameIfAbsent(user);

        log.info("creating user {}", user);
        userStorage.save(user);
        return user;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        checkUserId(id);
        return userStorage.getById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Validated(Marker.OnUpdate.class)
    public User updateUser(@Valid User user) {
        checkUserId(user.getId());

        if (userStorage.getById(user.getId()).isEmpty()) {
            throw new UserNotFoundException(user.getId());
        }

        userStorage.save(user);
        return user;
    }

    public int deleteAllUsers() {
        return userStorage.deleteAll();
    }

    public void deleteUserById(long userId) {
        checkUserId(userId);
        userStorage.getById(userId)
            .ifPresent(userStorage::delete);
    }

    public User addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException(friendId));

        user.addFriend(friend);
        friend.addFriend(user);

        userStorage.save(user);
        userStorage.save(friend);

        return user;
    }

    public Collection<User> getFriends(long userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getFriendsId().stream().map(this::getUserById).collect(Collectors.toList());
    }

    public void removeFromFriends(long userId, long friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException(friendId));

        user.removeFriend(friend);
        friend.removeFriend(user);
    }

    public Collection<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        User otherUser = userStorage.getById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(otherUserId));

        Set<Long> intersectSet = new HashSet<>(user.getFriendsId());
        intersectSet.retainAll(otherUser.getFriendsId());

        return intersectSet.stream().map(this::getUserById).toList();
    }

    private void setNameIfAbsent(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private void checkUserId(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException(null);
        }
    }
}
