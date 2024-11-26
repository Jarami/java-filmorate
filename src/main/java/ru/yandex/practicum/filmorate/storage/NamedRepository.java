package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class NamedRepository<T> {

    protected final NamedParameterJdbcTemplate namedTemplate;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Map<String, Object> params) {
        try {
            T result = namedTemplate.queryForObject(query, params, mapper);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Map<String, Object> params) {
        return namedTemplate.query(query, params, mapper);
    }

    protected int update(String query, Map<String, Object> params) {
        return namedTemplate.update(query, params);
    }

    protected KeyHolder insert(String query, Map<String, Object> params, String[] keyColumnNames) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameterSource = new MapSqlParameterSource(params);
        namedTemplate.update(query, parameterSource, keyHolder, keyColumnNames);
        return keyHolder;
    }
}
