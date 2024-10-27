package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.After;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Validated
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
public class Film {
    private Integer id;

    @NotBlank(message = "Название фильма не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Описание фильма не должно быть больше, чем 200 символов")
    private String description;

    @After(after = "1895-12-28", message = "Релиз фильма не должен быть раньше {after}")
    private LocalDate releaseDate;

    @Positive
    private int duration;
}
