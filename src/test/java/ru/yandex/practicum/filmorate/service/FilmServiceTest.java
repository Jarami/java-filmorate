package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dto.FilmGenreDto;
import ru.yandex.practicum.filmorate.dto.FilmMpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertEmpty;
import static ru.yandex.practicum.filmorate.util.TestUtil.assertFilmEquals;

// Эти тесты не учитывают валидации

public class FilmServiceTest {

    FilmService filmService;
    FilmStorage filmStorage;
    FilmMpaStorage filmMpaStorage;
    FilmGenreStorage filmGenreStorage;

    @BeforeEach
    void setup() {
        filmStorage = new InMemoryFilmStorage();
        filmMpaStorage = new InMemoryFilmMpaStorage();
        filmGenreStorage = new InMemoryFilmGenreStorage();
        filmService = new FilmService(filmStorage, filmMpaStorage, filmGenreStorage);
    }

    @Nested
    class CreateTests {
        @Test
        void givenFilmCreateRequest_whenCreate_getCreated() {
            NewFilmRequest filmToCreate = parseFilm("name;desc;G;2024-01-01;120");
            filmToCreate.setMpa(new FilmMpaDto(1));
            filmToCreate.setGenres(List.of(new FilmGenreDto(1)));

            Film film = filmService.createFilm(filmToCreate);

            assertNotNull(film.getId());
            assertFilmEquals(FilmMapper.mapToFilm(filmToCreate), film);
        }
    }

    @Nested
    class ReadTests {
        @Test
        void givenFilms_whenGetAll_getFilms() {
            Map<String, Film> films = new HashMap<>();
            films.put("name1", createFilm("name1;desc1;2024-01-01;120"));
            films.put("name2", createFilm("name2;desc2;2024-02-01;180"));

            List<Film> actualFilms = filmService.getAllFilms();

            actualFilms.forEach(actualFilm -> {
                Film film = films.get(actualFilm.getName());
                assertFilmEquals(film, actualFilm);
            });
        }

        @Test
        void givenExistingFilmId_whenGetById_getFilm() {
            Film existingFilm = createFilm("name1;desc1;2024-01-01;120");

            Film film = filmService.getFilmById(existingFilm.getId());

            assertEquals(existingFilm.getId(), film.getId());
            assertFilmEquals(existingFilm, film);
        }

        @Test
        void givenNonExistingFilm_whenGetById_getFilmNotFound() {
            Film film = new Film(1L, "name", "desc", "G",
                    LocalDate.parse("2024-01-01"), 120);

            assertThrows(NotFoundException.class, () -> filmService.getFilmById(film.getId()));
        }
    }

    @Nested
    class UpdateTests {
        @Test
        void givenExistingFilm_whenUpdate_getUpdated() {
            Film film1 = createFilm("name1;desc1;2024-01-01;120");

            UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
                    .id(film1.getId())
                    .name("name2")
                    .description("desc2")
                    .mpa(new FilmMpaDto(1))
                    .releaseDate(LocalDate.parse("2024-02-02"))
                    .duration(180)
                    .build();

            filmService.updateFilm(updateFilmRequest);

            Film actualUser = filmService.getFilmById(film1.getId());
            assertFilmEquals(FilmMapper.mapToFilm(updateFilmRequest), actualUser);
        }

        @Test
        void givenNonExistingFilm_whenUpdate_getFilmNotFoundr() {

            UpdateFilmRequest updateFilmRequest = UpdateFilmRequest.builder()
                    .id(1L)
                    .name("name")
                    .description("desc")
                    .mpa(new FilmMpaDto(1))
                    .releaseDate(LocalDate.parse("2024-01-01"))
                    .duration(120)
                    .build();

            assertThrows(NotFoundException.class, () -> filmService.updateFilm(updateFilmRequest));
        }
    }

    @Nested
    class DeleteTests {
        @Test
        void givenUsers_whenDeleteAll_getDeleted() {
            createFilm("name1;desc1;2024-01-01;120");
            createFilm("name2;desc2;2024-02-01;180");

            filmService.deleteAllFilms();

            assertEmpty(filmService.getAllFilms());
        }

        @Test
        void givenFilm_whenDelete_getDeleted() {
            Film film1 = createFilm("name1;desc1;2024-01-01;120");
            Film film2 = createFilm("name2;desc2;2024-02-01;180");

            filmService.deleteFilmById(film1.getId());

            List<Film> actualFilms = filmService.getAllFilms();
            List<Film> expectedFilms = List.of(film2);

            assertIterableEquals(expectedFilms, actualFilms);
        }

        @Test
        void givenNonExistingFilm_whenDelete_getFilmNotFound() {
            Film film = new Film(1L, "name", "desc", "G",
                    LocalDate.parse("2024-01-01"), 120);

            assertThrows(NotFoundException.class, () -> filmService.deleteFilmById(film.getId()));
        }
    }

    // Film filmToCreate = parseFilm("name;desc;G;2024-01-01;120");
    private NewFilmRequest parseFilm(String filmString) {
        String[] chunks = filmString.split(";");

        return NewFilmRequest.builder()
                .name(chunks[0].equals("NULL") ? null : chunks[0])
                .description(chunks[1].equals("NULL") ? null : chunks[1])
                .mpa(new FilmMpaDto(0))
                .genres(new ArrayList<>())
                .build();
    }

    private Film createFilm(String filmString) {
        NewFilmRequest request = parseFilm(filmString);
        request.setMpa(new FilmMpaDto(1));
        request.setGenres(List.of(new FilmGenreDto(1)));
        return filmService.createFilm(request);
    }
}
