package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class FilmReviewDto {
    private Long reviewId;

    private Long filmId;

    private Long userId;

    private String content;

    @JsonProperty("isPositive")
    private boolean isPositive;

    private int useful;
}
