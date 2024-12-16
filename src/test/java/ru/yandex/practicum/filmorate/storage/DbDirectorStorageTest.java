package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.util.TestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DbDirectorStorage.class, DirectorRowMapper.class})
public class DbDirectorStorageTest {

    private final DbDirectorStorage directorStorage;

    @AfterEach()
    void tearDown() {
        directorStorage.deleteAllDirectors();
    }

    @Test
    void successfulInsertAndSelectOne() {
        Director director = Director.builder().name(TestUtil.randomString(25)).build();
        Director savedDirector = directorStorage.saveDirector(director);
        Optional<Director> selectDirector = directorStorage.getDirectorById(savedDirector.getId());

        assertTrue(selectDirector.isPresent());
        assertEquals(director.getName(), selectDirector.get().getName());
    }

    @Test
    void successfulInsertAndSelectMany() {
        List<Director> directors = new ArrayList<>();
        directors.add(Director.builder().name(TestUtil.randomString(5)).build());
        directors.add(Director.builder().name(TestUtil.randomString(15)).build());
        directors.add(Director.builder().name(TestUtil.randomString(25)).build());
        directors.forEach(directorStorage::saveDirector);

        List<Director> savedDirectors = directorStorage.getAllDirectors();

        assertEquals(directors.size(), savedDirectors.size());
        assertEquals(directors.get(0).getName(), savedDirectors.get(0).getName());
        assertEquals(directors.get(1).getName(), savedDirectors.get(1).getName());
        assertEquals(directors.get(2).getName(), savedDirectors.get(2).getName());
    }


    @Test
    void successfulUpdate() {
        Director director = Director.builder().name(TestUtil.randomString(35)).build();
        director = directorStorage.saveDirector(director);

        director.setName(TestUtil.randomString(5));

        directorStorage.saveDirector(director);

        Optional<Director> optionalDirector = directorStorage.getDirectorById(director.getId());

        assertTrue(optionalDirector.isPresent());
        assertEquals(director.getName(), optionalDirector.get().getName());
    }

    @Test
    void successfulDelete() {
        List<Director> directors = new ArrayList<>();
        directors.add(Director.builder().name(TestUtil.randomString(5)).build());
        directors.add(Director.builder().name(TestUtil.randomString(15)).build());
        directors.forEach(directorStorage::saveDirector);

        directorStorage.deleteDirector(directors.get(0));
        List<Director> loadedDirectors = directorStorage.getAllDirectors();

        assertEquals(directors.size() - 1, loadedDirectors.size());
    }
}
