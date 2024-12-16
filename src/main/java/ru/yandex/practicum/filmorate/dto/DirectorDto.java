package ru.yandex.practicum.filmorate.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDto {
    private Integer id;
    private String name;
}
