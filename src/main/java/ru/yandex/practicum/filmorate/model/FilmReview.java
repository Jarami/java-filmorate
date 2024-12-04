package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class FilmReview {
    private Long reviewId;

    private long filmId;

    private long userId;

    private String content;

    @JsonProperty("isPositive")
    private boolean isPositive;

    @JsonProperty("useful")
    private int rate;
}
