package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ResponseDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{id}/friends")
public class FriendController {

    private final UserService userService;

    @GetMapping(value = {"", "/"})
    public List<UserDto> getFriends(@PathVariable Long id) {
        return userService.getFriends(id).stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    @GetMapping("/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId).stream()
                .map(UserMapper::mapToDto)
                .toList();
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
