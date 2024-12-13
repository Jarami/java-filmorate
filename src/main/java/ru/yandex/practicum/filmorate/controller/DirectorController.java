package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<DirectorDto> getAll() {
        return directorService.getAll().stream()
                .map(DirectorMapper::mapToDto)
                .toList();
    }

    @GetMapping("/{id}")
    public DirectorDto getDirectorById(@PathVariable Integer id) {
        Director director = directorService.getById(id);
        return DirectorMapper.mapToDto(director);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto createDirector(@Valid @RequestBody NewDirectorRequest newDirectorRequest) {
        Director director = directorService.createDirector(newDirectorRequest);
        return DirectorMapper.mapToDto(director);
    }

    @PutMapping
    public DirectorDto updateDirector(@Valid @RequestBody UpdateDirectorRequest updateDirectorRequest) {
        Director director = directorService.updateDirector(updateDirectorRequest);
        return DirectorMapper.mapToDto(director);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Integer id) {
        directorService.deleteDirectorId(id);
    }

    @DeleteMapping()
    public void deleteAllDirectors() {
        directorService.deleteAll();
    }
}
