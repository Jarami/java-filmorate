package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.validators.After;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class NewFilmRequest {

    @NotBlank(message = "Название фильма не должно быть пустым")
    private String name;

    @Size(
            max = 200,
            message = "Описание фильма не должно быть больше, чем 200 символов")
    private String description;

    @After(
            after = "1895-12-28",
            message = "Релиз фильма не должен быть раньше {after}")
    private LocalDate releaseDate;

    @Positive
    private int duration;

    @NotNull
    @JsonProperty("mpa")
    private FilmMpaDto mpa;

    private List<FilmGenreDto> genres;
}
