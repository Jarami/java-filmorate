package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ResponseDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{id}/friends")
public class FriendController {

    private final UserService userService;

    @GetMapping(value = {"", "/"})
    public Collection<User> getFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PutMapping("/{friendId}")
    public ResponseDto addFriend(@PathVariable(value = "id") long userId,
                                 @PathVariable(value = "friendId") long friendId) {

        boolean result = userService.addFriend(userId, friendId);
        return new ResponseDto(result);
    }

    @DeleteMapping("/{friendId}")
    public ResponseDto removeFriend(@PathVariable(value = "id") long userId1,
                               @PathVariable(value = "friendId") long userId2) {

        boolean result = userService.removeFromFriends(userId1, userId2);
        return new ResponseDto(result);
    }
}
