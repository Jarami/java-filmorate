package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDirectorRequest {
    @NotBlank(message = "Имя режиссера не может быть пустым")
    @Size(max = 50,
            message = "Максимальная длина имени режиссера 50 символов")
    private String name;
}