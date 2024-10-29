package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exceptions.UserNotFound;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    @Validated(Marker.OnCreate.class)
    public User createUser(@Valid User user) {
        log.info("creating user {}", user);
        setNameIfAbsent(user);
        userStorage.save(user);
        return user;
    }

    public Collection<User> getAllUsers() {
        log.info("getting all user");
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        checkUserId(id);
        return userStorage.getById(id);
    }

    @Validated(Marker.OnUpdate.class)
    public User updateUser(@Valid User user) {
        log.info("updating user {}", user);
        checkUserId(user.getId());
        setNameIfAbsent(user);
        userStorage.save(user);
        return user;
    }

//    @Validated(Marker.OnDelete.class)
//    public void deleteUser(@Valid User user) {
//        log.info("deleting user {}", user);
//        checkUserId(user.getId());
//        userStorage.delete(user);
//    }

    public int deleteAllUsers() {
        log.info("deleting all users");
        return userStorage.deleteAll();
    }

    public void deleteUserById(long userId) {
        log.info("deleting user with id = {}", userId);
        checkUserId(userId);
        userStorage.delete(userStorage.getById(userId));
    }

    public User addFriend(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user == null) throw new UserNotFound(userId);
        if (friend == null) throw new UserNotFound(friendId);

        user.addFriend(friend);
        friend.addFriend(user);

        userStorage.save(user);
        userStorage.save(friend);

        return user;
    }

    public Collection<User> getFriends(long userId) {
        User user = userStorage.getById(userId);

        if (user == null) throw new UserNotFound(userId);

        return user.getFriendsId().stream().map(this::getUserById).collect(Collectors.toList());
    }

    public void removeFromFriends(long userId, long friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);

        if (user == null) throw new UserNotFound(userId);
        if (friend == null) throw new UserNotFound(friendId);

        user.removeFriend(friend);
        friend.removeFriend(user);
    }

    public Collection<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.getById(userId);
        User otherUser = userStorage.getById(otherUserId);

        if (user == null) throw new UserNotFound(userId);
        if (otherUser == null) throw new UserNotFound(otherUserId);

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
        if (userId == null || userStorage.getById(userId) == null) {
            throw new UserNotFound(userId);
        }
    }
}
