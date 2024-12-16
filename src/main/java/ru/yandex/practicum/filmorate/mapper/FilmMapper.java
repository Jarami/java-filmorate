package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {

        List<Director> directors = new ArrayList<>();
        if (request.getDirectors() != null) {
            directors = DirectorMapper.mapToDirector(request.getDirectors());
        }

        List<FilmGenre> genres = new ArrayList<>();
        if (request.getGenres() != null) {
            genres = FilmGenreMapper.mapToGenre(request.getGenres());
        }

        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .directors(directors)
                .genres(genres)
                .build();
    }

    public static NewFilmRequest mapToNewFilmRequest(Film film) {
        return NewFilmRequest.builder()
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(FilmMpaMapper.mapToDto(film.getMpa()))
                .directors(DirectorMapper.mapToDto(film.getDirectors()))
                .genres(FilmGenreMapper.mapToDto(film.getGenres()))
                .build();
    }

    public static Film mapToFilm(UpdateFilmRequest request) {

        List<Director> directors = new ArrayList<>();
        if (request.getDirectors() != null) {
            directors = DirectorMapper.mapToDirector(request.getDirectors());
        }

        List<FilmGenre> genres = new ArrayList<>();
        if (request.getGenres() != null) {
            genres = FilmGenreMapper.mapToGenre(request.getGenres());
        }

        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .directors(directors)
                .genres(genres)
                .build();
    }

    public static UpdateFilmRequest mapToUpdateFilmRequest(Film film) {
        return UpdateFilmRequest.builder()
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(FilmMpaMapper.mapToDto(film.getMpa()))
                .directors(DirectorMapper.mapToDto(film.getDirectors()))
                .genres(FilmGenreMapper.mapToDto(film.getGenres()))
                .build();
    }

    public static FilmDto mapToDto(Film film) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(FilmMpaMapper.mapToDto(film.getMpa()))
                .genres(FilmGenreMapper.mapToDto(film.getGenres()))
                .directors(DirectorMapper.mapToDto(film.getDirectors()))
                .rate(film.getRate())
                .build();
    }

    public static Film mapToFilm(FilmDto dto) {
        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(FilmMpaMapper.mapToMpa(dto.getMpa()))
                .genres(FilmGenreMapper.mapToGenre(dto.getGenres()))
                .directors(DirectorMapper.mapToDirector(dto.getDirectors()))
                .rate(dto.getRate())
                .build();
    }

    public static List<FilmDto> mapToDto(List<Film> films) {
        return films.stream().map(FilmMapper::mapToDto).toList();
    }
}
