package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
public class UpdateDirectorRequest {
    @NotNull
    private Integer id;
    @NotBlank
    private String name;
}
