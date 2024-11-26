package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(
            @Qualifier("db") UserStorage userStorage,
            @Qualifier("db") FriendshipStorage friendshipStorage) {

        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public User createUser(@Valid NewUserRequest newUserRequest) {
        setNameIfAbsent(newUserRequest);

        log.info("creating user {}", newUserRequest);

        return userStorage.save(UserMapper.mapToUser(newUserRequest));
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        checkUserId(id);
        return userStorage.getById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUser(@Valid UpdateUserRequest updateUserRequest) {

        if (userStorage.getById(updateUserRequest.getId()).isEmpty()) {
            throw new UserNotFoundException(updateUserRequest.getId());
        }

        User user = UserMapper.mapToUser(updateUserRequest);

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

    public Collection<User> getFriends(long userId) {
        User user = getById(userId);

        return friendshipStorage.getFriends(user).stream().map(this::getUserById).toList();
    }

    public Collection<User> getCommonFriends(long userId, long otherUserId) {
        User user = getById(userId);
        User otherUser = getById(otherUserId);

        return friendshipStorage.getCommonFriends(user, otherUser).stream()
                .map(this::getUserById).toList();
    }

    public boolean addFriend(long userId, long friendId) {

        User user = getById(userId);
        User friend = getById(friendId);

        log.debug("making friends: {} and {}", user.getLogin(), friend.getLogin());

        return friendshipStorage.addFriend(user, friend);
    }

    public boolean removeFromFriends(long userId1, long userId2) {
        User user1 = getById(userId1);
        User user2 = getById(userId2);

        return friendshipStorage.removeFriend(user1, user2);
    }

    private void setNameIfAbsent(NewUserRequest user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private User getById(Long userId) {
        return userStorage.getById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void checkUserId(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException(null);
        }
    }
}
