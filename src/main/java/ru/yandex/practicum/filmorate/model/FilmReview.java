package ru.yandex.practicum.filmorate.model;

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

    private boolean isPositive;

    private int rate;
}
