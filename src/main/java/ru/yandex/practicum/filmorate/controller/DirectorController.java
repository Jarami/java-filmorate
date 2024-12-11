package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping("")
    public List<Director> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        return directorService.getById(id);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Director createDirector(@Valid @RequestBody NewDirectorRequest newDirectorRequest) {
        return directorService.createDirector(newDirectorRequest);
    }

    @PutMapping("")
    public Director updateDirector(@Valid @RequestBody UpdateDirectorRequest updateDirectorRequest) {
        return directorService.updateDirector(updateDirectorRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable int id) {
        directorService.deleteDirectorId(id);
    }

    @DeleteMapping()
    public void deleteAllDirectors() {
        directorService.deleteAll();
    }
}
