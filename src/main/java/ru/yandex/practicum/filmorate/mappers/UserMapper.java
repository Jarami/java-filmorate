package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {
    private UserMapper() {

    }

    public static User fromDto(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getEmail(),
                userDto.getLogin(),
                userDto.getName(),
                userDto.getBirthday()
        );
    }

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
    }
}
