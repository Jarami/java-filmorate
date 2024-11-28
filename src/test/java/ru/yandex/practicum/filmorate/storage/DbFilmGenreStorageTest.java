package ru.yandex.practicum.filmorate.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmGenre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmGenreRowMapper;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ FilmGenreRowMapper.class, DbFilmGenreStorage.class })
public class DbFilmGenreStorageTest {

    private final DbFilmGenreStorage filmGenreStorage;

    @BeforeEach
    void setup() {
        filmGenreStorage.deleteAll();
        filmGenreStorage.save(new FilmGenre(null, "Комедия"));
        filmGenreStorage.save(new FilmGenre(null, "Драма"));
        filmGenreStorage.save(new FilmGenre(null, "Мультфильм"));
    }

    @AfterEach
    void shutdown() {
        filmGenreStorage.deleteAll();
    }

    @Test
    void givenExistingGenre_whenDelete_gotDeleted() {
        List<FilmGenre> genres = filmGenreStorage.getAll();

        filmGenreStorage.delete(genres.get(0));

        List<FilmGenre> expGenres = genres.subList(1, 3);
        List<FilmGenre> actGenres = filmGenreStorage.getAll();

        assertEquals(2, actGenres.size());

        Set<Integer> expIds = expGenres.stream().map(FilmGenre::getId).collect(Collectors.toSet());
        Set<Integer> actIds = actGenres.stream().map(FilmGenre::getId).collect(Collectors.toSet());
        assertEquals(expIds, actIds);
    }

    @Test
    void givenGenres_whenGetAll_gotAll() {
        List<FilmGenre> genres = filmGenreStorage.getAll();

        assertEquals(3, genres.size());
        assertNotNull(genres.get(0).getId());
        assertNotNull(genres.get(1).getId());
        assertNotNull(genres.get(2).getId());
    }

    @Test
    void givenExistingGenre_whenGetById_gotIt() {
        List<FilmGenre> genres = filmGenreStorage.getAll();
        FilmGenre expectedGenre = genres.get(0);
        int id = expectedGenre.getId();

        FilmGenre actualGenre = filmGenreStorage.getById(id)
            .orElseThrow(() -> new NotFoundException("no genre", "no genre by id = " + id));

        assertEquals(expectedGenre.getName(), actualGenre.getName());
    }

    @Test
    void givenNonExistingGenre_whenGetById_gotNothing() {
        Integer nonExistingId = filmGenreStorage.getAll().stream()
            .map(FilmGenre::getId).max(Integer::compare).get() + 1;

        Optional<FilmGenre> actualGenre = filmGenreStorage.getById(nonExistingId);

        assertTrue(actualGenre.isEmpty());
    }

    @Test
    void givenExistingGenres_whenGetById_gotIt() {
        List<FilmGenre> genres = filmGenreStorage.getAll();
        List<FilmGenre> expGenres = genres.subList(0, 2);
        List<Integer> ids = expGenres.stream().map(FilmGenre::getId).toList();

        List<FilmGenre> actGenres = filmGenreStorage.getById(ids);

        assertEquals(expGenres.size(), actGenres.size());

        Map<Integer, FilmGenre> actGenreById = new HashMap<>();
        actGenres.forEach(g -> actGenreById.put(g.getId(), g));

        expGenres.forEach(expGenre -> {
            FilmGenre actGenre = actGenreById.get(expGenre.getId());
            assertNotNull(actGenre);
            assertEquals(expGenre.getName(), actGenre.getName());
        });
    }

    @Test
    void givenExistingGenre_whenUpdate_gotUpdated() {
        List<FilmGenre> genres = filmGenreStorage.getAll();
        Integer id = genres.get(0).getId();
        FilmGenre updatedGenre = new FilmGenre(id, genres.get(1).getName());

        filmGenreStorage.save(updatedGenre);

        FilmGenre actGenre = filmGenreStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("no genre", "no genre by id = " + id));

        assertEquals(genres.get(1).getName(), actGenre.getName());
    }
}
