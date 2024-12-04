package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class UpdateFilmReviewRequest {
    @NotNull
    private Long reviewId;
    @NotNull
    private Long filmId;
    @NotNull
    private Long userId;
    @NotBlank(message = "Содержимое отзыва не должно быть пустым")
    private String content;
    private Boolean isPositive;
}
