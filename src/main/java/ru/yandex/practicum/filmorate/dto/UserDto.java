package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
