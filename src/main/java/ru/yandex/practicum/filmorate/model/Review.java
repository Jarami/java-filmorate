package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class Review {
    private Long id;
    private long filmId;
    private long userId;
    private String content;
    private boolean isPositive;
    private int rate;
}
