package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class UpdateFilmRequest {

    @NotNull
    private Long id;

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
    private Integer duration;

    @JsonProperty("mpa")
    private FilmMpaDto mpa;

    private List<FilmGenreDto> genres;
    private List<DirectorDto> directors;
}
