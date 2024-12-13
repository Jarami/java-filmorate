package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.UserLogin;

import java.time.LocalDate;

@Data
@Slf4j
@Builder
@ToString
@Validated
@AllArgsConstructor
public class UpdateUserRequest {

    @NotNull
    private Long id;

    @Email
    private String email;

    @UserLogin(message = "Логин должен быть заполнен и не должен содержать пробелов")
    private String login;

    @Size(max = 256)
    private String name;

    @Past
    private LocalDate birthday;
}
