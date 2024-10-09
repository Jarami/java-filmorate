package ru.yandex.practicum.filmorate.config;

import java.time.LocalDate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@ConfigurationProperties(prefix = "film")
@Getter
@Setter
@ToString
public class FilmConfig {
    private Integer maxDescSize;
    private LocalDate minReleaseDate;
}
