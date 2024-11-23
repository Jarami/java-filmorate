package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.validators.Marker;
import ru.yandex.practicum.filmorate.validators.UserLogin;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
public class NewUserRequest {

    @Email
    private String email;

    @UserLogin(message = "Логин должен быть заполнен и не должен содержать пробелов")
    private String login;

    @Size(max = 256)
    private String name;

    @Past
    private LocalDate birthday;
}
