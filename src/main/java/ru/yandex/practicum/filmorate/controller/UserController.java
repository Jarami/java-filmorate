package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EventService eventService;

    @GetMapping(value = {"", "/"})
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(UserMapper::mapToDto)
                .toList();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return UserMapper.mapToDto(user);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable Long id) {
        return userService.getRecommendations(id)
                .stream()
                .map(FilmMapper::mapToDto)
                .toList();
    }

    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody NewUserRequest newUserRequest) {
        User user = userService.createUser(newUserRequest);
        return UserMapper.mapToDto(user);
    }

    @PutMapping(value = {"", "/"})
    public UserDto updateUser(@RequestBody UpdateUserRequest updateUserRequest) {
        User user = userService.updateUser(updateUserRequest);
        return UserMapper.mapToDto(user);
    }

    @DeleteMapping(value = {"", "/"})
    public int deleteUsers() {
        return userService.deleteAllUsers();
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @GetMapping("/{id}/feed")
    public List<EventDto> getFeed(@PathVariable  Long id) {
        User user = userService.getUserById(id);
        return eventService.findEventsByUser(user).stream()
                .map(EventMapper::mapToDto)
                .toList();
    }
}
