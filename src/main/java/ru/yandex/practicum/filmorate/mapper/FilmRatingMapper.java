package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmRatingDto;
import ru.yandex.practicum.filmorate.model.FilmRating;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmRatingMapper {

    public static FilmRatingDto mapToDto(FilmRating rating) {
        return new FilmRatingDto(rating.getId());
    }

}
