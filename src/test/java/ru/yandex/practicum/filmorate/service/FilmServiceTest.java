package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;
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

    @BeforeEach
    void setup() {
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(filmStorage);
    }

    @Nested
    class CreateTests {
        @Test
        void givenFilmCreateRequest_whenCreate_getCreated() {
            Film filmToCreate = parseFilm("name;desc;G;2024-01-01;120");

            Film film = filmService.createFilm(filmToCreate);

            assertNotNull(film.getId());
            assertFilmEquals(filmToCreate, film);
        }
    }

    @Nested
    class ReadTests {
        @Test
        void givenFilms_whenGetAll_getFilms() {
            Map<String, Film> films = new HashMap<>();
            films.put("name1", createFilm("name1;desc1;2024-01-01;120"));
            films.put("name2", createFilm("name2;desc2;2024-02-01;180"));

            Collection<Film> actualFilms = filmService.getAllFilms();

            actualFilms.forEach(actualFilm -> {
                Film film = films.get(actualFilm.getTitle());
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

            assertThrows(FilmNotFoundException.class, () -> filmService.getFilmById(film.getId()));
        }
    }

    @Nested
    class UpdateTests {
        @Test
        void givenExistingFilm_whenUpdate_getUpdated() {
            Film film1 = createFilm("name1;desc1;2024-01-01;120");

            Film updatedFilm = new Film(film1.getId(), "name2", "desc2", "G",
                    LocalDate.parse("2024-02-02"), 180);

            filmService.updateFilm(updatedFilm);

            Film actualUser = filmService.getFilmById(film1.getId());
            assertFilmEquals(updatedFilm, actualUser);
        }

        @Test
        void givenNonExistingFilm_whenUpdate_getFilmNotFoundr() {
            Film film = new Film(1L, "name", "desc", "G",
                    LocalDate.parse("2024-01-01"), 120);

            assertThrows(FilmNotFoundException.class, () -> filmService.updateFilm(film));
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

            Collection<Film> actualFilms = filmService.getAllFilms();
            Collection<Film> expectedFilms = List.of(film2);

            assertIterableEquals(expectedFilms, actualFilms);
        }

        @Test
        void givenNonExistingFilm_whenDelete_getFilmNotFound() {
            Film film = new Film(1L, "name", "desc", "G",
                    LocalDate.parse("2024-01-01"), 120);

            assertThrows(FilmNotFoundException.class, () -> filmService.deleteFilmById(film.getId()));
        }
    }

    private Film parseFilm(String filmString) {
        String[] chunks = filmString.split(";");
        return new Film(
                null,
                chunks[0].equals("NULL") ? null : chunks[0],
                chunks[1].equals("NULL") ? null : chunks[1],
                chunks[2],
                chunks[3].equals("NULL") ? null : LocalDate.parse(chunks[3]),
                Integer.parseInt(chunks[4])
        );
    }

    private Film createFilm(String filmString) {
        return filmService.createFilm(parseFilm(filmString));
    }
}
