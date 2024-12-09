package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
public class NewFilmReviewRequest {
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    @NotBlank(message = "Содержимое отзыва не должно быть пустым")
    private String content;
    @NotNull
    private Boolean isPositive;
}
