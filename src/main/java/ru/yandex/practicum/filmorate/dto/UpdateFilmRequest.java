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
import ru.yandex.practicum.filmorate.mapper.FilmGenreMapper;
import ru.yandex.practicum.filmorate.mapper.FilmRatingMapper;
import ru.yandex.practicum.filmorate.model.Film;
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
    private FilmRatingDto rating;

    private List<FilmGenreDto> genres;

    public static UpdateFilmRequest.UpdateFilmRequestBuilder from(Film film) {
        return builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .rating(FilmRatingMapper.mapToDto(film.getRating()))
                .genres(FilmGenreMapper.mapToDto(film.getGenres()));

    }
}
