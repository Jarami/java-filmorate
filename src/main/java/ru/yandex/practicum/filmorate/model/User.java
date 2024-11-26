package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Data
@ToString
@Validated
@Slf4j
@Builder
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String login;

    private String name;
    private LocalDate birthday;

    public User() {
        this(null, null, null, null, null);
    }

    public User(String email, String login, String name, LocalDate birthday) {
        this(null, email, login, name, birthday);
    }
}
