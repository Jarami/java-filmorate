package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
public class UpdateDirectorRequest {
    @NotNull(message = "При обновлении идентификатор обязателен")
    private Integer id;
    @NotBlank(message = "Имя режиссера не может быть пустым")
    @Size(max = 50,
            message = "Максимальная длина имени режиссера 50 символов")
    private String name;
}
