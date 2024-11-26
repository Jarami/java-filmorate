package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Arrays;
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

    protected <K> K queryForObject(String query, Map<String, Object> params, Class<K> clazz) {
        return namedTemplate.queryForObject(query, params, clazz);
    }

    protected List<T> findMany(String query, Map<String, Object> params) {
        return namedTemplate.query(query, params, mapper);
    }

    protected <K> List<K> findMany(String query, Map<String, Object> params, RowMapper<K> currentMapper) {
        return namedTemplate.query(query, params, currentMapper);
    }

    protected List<T> getAll(String query) {
        return namedTemplate.query(query, mapper);
    }

    protected int delete(String query) {
        return delete(query, Map.of());
    }

    protected int delete(String query, Map<String, Object> params) {
        return namedTemplate.update(query, params);
    }

    protected int update(String query, Map<String, Object> params) {
        return namedTemplate.update(query, params);
    }

    protected int batchUpdate(String query, List<Map<String, Object>> batchValues) {
        Map<String, Object>[] array = new Map[batchValues.size()];
        batchValues.toArray(array);

        int[] result = namedTemplate.batchUpdate(query, array);
        return Arrays.stream(result).sum();
    }

    protected KeyHolder insert(String query, Map<String, Object> params, String[] keyColumnNames) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameterSource = new MapSqlParameterSource(params);
        namedTemplate.update(query, parameterSource, keyHolder, keyColumnNames);
        return keyHolder;
    }
}
