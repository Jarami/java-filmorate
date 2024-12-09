package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> getAll() {
        return directorStorage.getAllDirectors();
    }

    public Director getById(int id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Не найден режиссер", "не найден режиссер с id=" + id));
    }

    public Director createDirector(Director director) {
        Director directorToSave = new Director().builder()
                .name(director.getName())
                .build();
        log.info("Запись нового режиссера {}", directorToSave.getName());
        return directorStorage.saveDirector(directorToSave);
    }

    public Director updateDirector(UpdateDirectorRequest updateDirectorRequest) {
        log.info("Обновление режиссера {}", updateDirectorRequest);
        Director directorToUpdate = getById(updateDirectorRequest.getId());
        directorToUpdate.setName(updateDirectorRequest.getName());
        return directorStorage.saveDirector(directorToUpdate);
    }

    public void deleteDirectorId(int id) {
        Director director = this.getById(id);
        directorStorage.deleteDirector(director);
    }
}
