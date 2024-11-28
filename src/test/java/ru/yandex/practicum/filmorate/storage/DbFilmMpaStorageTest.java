package ru.yandex.practicum.filmorate.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
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
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.FilmMpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMpaRowMapper;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ DbFilmMpaStorage.class, FilmMpaRowMapper.class })
public class DbFilmMpaStorageTest {

    private final DbFilmMpaStorage filmMpaStorage;

    private FilmMpa mpa1;
    private FilmMpa mpa2;

    @BeforeEach
    void setup() {
        shutdown();

        mpa1 = filmMpaStorage.save(new FilmMpa(null, "G"));
        mpa2 = filmMpaStorage.save(new FilmMpa(null, "PG"));
    }

    @AfterEach
    void shutdown() {
        filmMpaStorage.deleteAll();
    }

    @Test
    void givenExistingMpa_whenDelete_gotDeleted() {
        filmMpaStorage.delete(mpa1);

        List<FilmMpa> actMpa = filmMpaStorage.getAll();

        assertEquals(1, actMpa.size());
        assertEquals(mpa2.getName(), actMpa.get(0).getName());
    }

    @Test
    void givenMpa_whenGetAll_gotAll() {
        List<FilmMpa> mpa = filmMpaStorage.getAll();

        assertEquals(2, mpa.size());

        Set<String> actNames = mpa.stream().map(FilmMpa::getName).collect(Collectors.toSet());
        assertEquals(Set.of(mpa1.getName(), mpa2.getName()), actNames);
    }

    @Test
    void givenExistingMpa_whenGetById_gotIt() {
        FilmMpa actMpa = filmMpaStorage.getById(mpa1.getId()).get();

        assertEquals(mpa1.getName(), actMpa.getName());
    }

    @Test
    void givenExistingMpa_whenUpdate_gotUpdated() {
        mpa1.setName("PG-13");
        filmMpaStorage.save(mpa1);

        FilmMpa actMpa = filmMpaStorage.getById(mpa1.getId()).get();

        assertEquals("PG-13", actMpa.getName());
    }
}
