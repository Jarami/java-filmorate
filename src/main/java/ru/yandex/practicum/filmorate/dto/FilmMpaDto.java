package ru.yandex.practicum.filmorate.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FilmMpaDto {
    private int id;
    private String name;
}
