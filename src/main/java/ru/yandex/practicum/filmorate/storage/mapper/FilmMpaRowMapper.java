package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmMpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmMpaRowMapper implements RowMapper<FilmMpa> {
    @Override
    public FilmMpa mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return FilmMpa.builder()
                .id(resultSet.getInt("mpa_id"))
                .name(resultSet.getString("mpa_name"))
                .build();
    }
}
