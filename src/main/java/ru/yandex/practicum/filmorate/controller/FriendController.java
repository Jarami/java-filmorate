package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ResponseDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{id}/friends")
public class FriendController {

    private final UserService userService;

    @GetMapping(value = {"", "/"})
    public List<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PutMapping("/{friendId}")
    public ResponseDto addFriend(@PathVariable(value = "id") Long userId,
                                 @PathVariable(value = "friendId") Long friendId) {

        boolean result = userService.addFriend(userId, friendId);
        return new ResponseDto(result);
    }

    @DeleteMapping("/{friendId}")
    public ResponseDto removeFriend(@PathVariable(value = "id") Long userId1,
                               @PathVariable(value = "friendId") Long userId2) {

        boolean result = userService.removeFromFriends(userId1, userId2);
        return new ResponseDto(result);
    }
}
