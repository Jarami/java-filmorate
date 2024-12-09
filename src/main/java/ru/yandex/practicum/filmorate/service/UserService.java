package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@Validated
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventService eventService;

    public UserService(
            @Qualifier("db") UserStorage userStorage,
            @Qualifier("db") FriendshipStorage friendshipStorage,
            EventService eventService) {

        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
        this.eventService = eventService;
    }

    public User createUser(@Valid NewUserRequest newUserRequest) {
        setNameIfAbsent(newUserRequest);

        log.info("creating user {}", newUserRequest);

        return userStorage.save(UserMapper.mapToUser(newUserRequest));
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(long id) {
        return userStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь с id = " + id));
    }

    public User updateUser(@Valid UpdateUserRequest updateUserRequest) {

        Long userId = updateUserRequest.getId();

        User user = userStorage.getById(userId)
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь по id = " + userId));

        if (updateUserRequest.getName() != null) {
            user.setName(updateUserRequest.getName());
        }

        if (updateUserRequest.getLogin() != null) {
            user.setLogin(updateUserRequest.getLogin());
        }

        if (updateUserRequest.getEmail() != null) {
            user.setEmail(updateUserRequest.getEmail());
        }

        if (updateUserRequest.getBirthday() != null) {
            user.setBirthday(updateUserRequest.getBirthday());
        }

        log.debug("updating user {}", user);
        return userStorage.save(user);
    }

    public int deleteAllUsers() {
        return userStorage.deleteAll();
    }

    public void deleteUserById(long userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь с id = " + userId));

        userStorage.delete(user);
    }

    public List<User> getFriends(long userId) {
        User user = getById(userId);

        return friendshipStorage.getFriends(user).stream().map(this::getUserById).toList();
    }

    public List<User> getCommonFriends(long userId, long otherUserId) {
        User user = getById(userId);
        User otherUser = getById(otherUserId);

        return friendshipStorage.getCommonFriends(user, otherUser).stream()
                .map(this::getUserById).toList();
    }

    public List<Film> getRecommendations(long userId) {
        return filmStorage.getRecommendations(userId);
    }

    public boolean addFriend(long userId, long friendId) {

        User user = getById(userId);
        User friend = getById(friendId);

        log.debug("making friends: {} and {}", user.getLogin(), friend.getLogin());
        eventService.createAddFriendEvent(userId, friendId);

        return friendshipStorage.addFriend(user, friend);
    }

    public boolean removeFromFriends(long userId, long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        eventService.createRemoveFriendEvent(userId, friendId);
        return friendshipStorage.removeFriend(user, friend);
    }

    private void setNameIfAbsent(NewUserRequest user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    private User getById(Long userId) {
        return userStorage.getById(userId)
                .orElseThrow(() ->
                        new NotFoundException("не найден пользователь", "не найден пользователь с id = " + userId));
    }
}
