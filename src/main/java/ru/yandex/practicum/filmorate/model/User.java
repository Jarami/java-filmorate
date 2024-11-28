package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;

import org.springframework.validation.annotation.Validated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Validated
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String login;

    private String name;
    private LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this(null, email, login, name, birthday);
    }
}
