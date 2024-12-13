package ru.yandex.practicum.filmorate.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String login;

    private String name;
    private LocalDate birthday;
}
