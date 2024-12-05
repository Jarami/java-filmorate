package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.FilmMpaStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class DataLoader {

    private static final List<String> GENRE_NAMES =
            List.of("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");

    private static final List<String> MPA_NAMES = List.of("G", "PG", "PG-13", "R", "NC-17");

    private final FilmGenreStorage genreStorage;
    private final FilmMpaStorage mpaStorage;

    public DataLoader(
        @Qualifier("db") FilmMpaStorage mpaStorage,
        @Qualifier("db") FilmGenreStorage genreStorage) {

        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    @Bean
    public CommandLineRunner loadData() {

        return args -> {

            log.info("Loading genres and mpa");

            int genresLoaded = 0;
            int mpaLoaded = 0;

            Set<String> genreNames = genreStorage.getAll().stream().map(FilmGenre::getName).collect(Collectors.toSet());
            for (String name : GENRE_NAMES) {
                if (!genreNames.contains(name)) {
                    genreStorage.save(FilmGenre.builder().name(name).build());
                    genresLoaded++;
                }
            }

            Set<String> mpaNames = mpaStorage.getAll().stream().map(FilmMpa::getName).collect(Collectors.toSet());
            for (String name : MPA_NAMES) {
                if (!mpaNames.contains(name)) {
                    mpaStorage.save(FilmMpa.builder().name(name).build());
                    mpaLoaded++;
                }
            }

            log.info("Loading genres and mpa finish: genres {}, mpa {}", genresLoaded, mpaLoaded);
        };
    }
}
