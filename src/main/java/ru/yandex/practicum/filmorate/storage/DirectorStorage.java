package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Optional<Director> getDirectorById(int directorId);

    Director saveDirector(Director director);

    void deleteDirector(Director director);

    void deleteAllDirectors();

    List<Director> getById(List<Integer> directorIds);
}
