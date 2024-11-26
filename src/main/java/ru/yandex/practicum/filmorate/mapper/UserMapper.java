package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.User;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
    public static User mapToUser(NewUserRequest request) {

        return User.builder()
                .email(request.getEmail())
                .login(request.getLogin())
                .name(request.getName())
                .birthday(request.getBirthday())
                .build();
    }

    public static User mapToUser(UpdateUserRequest request) {

        return User.builder()
                .id(request.getId())
                .email(request.getEmail())
                .login(request.getLogin())
                .name(request.getName())
                .birthday(request.getBirthday())
                .build();
    }
}
