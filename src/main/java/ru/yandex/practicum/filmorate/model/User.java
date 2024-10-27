package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.UserLogin;

import java.time.LocalDate;

/**
 * User.
 */
@Data
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
@Validated
public class User {

    private Integer id;

    @Email
    private String email;

    @UserLogin(message = "Логин должен быть заполнен и не должен содержать пробелов")
    private String login;

    private String name;

    @Past
    private LocalDate birthday;
}
