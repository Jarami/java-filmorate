package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.validators.After;
import ru.yandex.practicum.filmorate.validators.Marker;

import java.time.LocalDate;

@Data
@Validated
@ToString
@EqualsAndHashCode(of = { "id" })
@AllArgsConstructor
public class Film {
    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = {Marker.OnUpdate.class, Marker.OnDelete.class})
    private Long id;

    @NotBlank(
            message = "Название фильма не должно быть пустым",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String name;

    @Size(
            max = 200,
            message = "Описание фильма не должно быть больше, чем 200 символов",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private String description;

    @After(
            after = "1895-12-28",
            message = "Релиз фильма не должен быть раньше {after}",
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private LocalDate releaseDate;

    @Positive(groups = {Marker.OnCreate.class, Marker.OnUpdate.class})
    private int duration;
}
