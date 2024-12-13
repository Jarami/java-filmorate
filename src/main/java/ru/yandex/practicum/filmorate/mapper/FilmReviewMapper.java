package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmReviewDto;
import ru.yandex.practicum.filmorate.model.FilmReview;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmReviewMapper {

    public static FilmReviewDto mapToDto(FilmReview review) {
        return FilmReviewDto.builder()
                .reviewId(review.getReviewId())
                .filmId(review.getFilmId())
                .userId(review.getUserId())
                .content(review.getContent())
                .isPositive(review.isPositive())
                .useful(review.getRate())
                .build();
    }

    public static FilmReview mapToReview(FilmReviewDto dto) {
        return FilmReview.builder()
                .reviewId(dto.getReviewId())
                .filmId(dto.getFilmId())
                .userId(dto.getUserId())
                .content(dto.getContent())
                .isPositive(dto.isPositive())
                .rate(dto.getUseful())
                .build();
    }
}
