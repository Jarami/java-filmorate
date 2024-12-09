package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FailedToCreateEntity;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("db")
public class DbDirectorStorage extends NamedRepository<Director> implements DirectorStorage {

    private static final String FIND_ALL = """
            SELECT  director_id,
            		name
            FROM DIRECTORS""";

    private static final String FIND_ONE_BY_ID = """
            SELECT  director_id,
            		name
            FROM DIRECTORS
            WHERE DIRECTOR_ID =:directorId""";

    private static final String INSERT_QUERY = """
            INSERT INTO DIRECTORS (NAME)
            VALUES (:name)""";

    private static final String UPDATE_QUERY = """
            UPDATE DIRECTORS
            SET NAME = :name
            WHERE DIRECTOR_ID = :directorId;""";

    private static final String DELETE_BY_ID = """
            DELETE FROM DIRECTORS
            WHERE director_id = :directorId""";

    public DbDirectorStorage(NamedParameterJdbcTemplate namedTemplate, RowMapper<Director> mapper) {
        super(namedTemplate, mapper);
    }

    @Override
    public List<Director> getAllDirectors() {
        return getAll(FIND_ALL);
    }

    @Override
    public Optional<Director> getDirectorById(int directorId) {
        return findOne(FIND_ONE_BY_ID, Map.of("directorId", directorId));
    }

    @Override
    public Director saveDirector(Director director) {
        if (director.getId() == null) {
            KeyHolder keyHolder = insert(
                    INSERT_QUERY,
                    Map.of("name", director.getName()),
                    new String[]{"director_id"}
            );
            Integer id = keyHolder.getKeyAs(Integer.class);
            if (id == null) {
                throw new FailedToCreateEntity("не удалось создать режиссера {} " + director.getName());
            } else {
                director.setId(id);
                log.info("Режиссер {} сохранен с id = {}", director.getName(), id);
            }
        } else {
            update(UPDATE_QUERY, Map.of("name", director.getName(), "directorId", director.getId()));
        }
        return director;
    }

    @Override
    public void deleteDirector(Director director) {
        delete(DELETE_BY_ID, Map.of("directorId", director.getId()));
    }

    @Override
    public List<Director> getById(List<Integer> directorIds) {
        return this.getAllDirectors().stream()
                .filter(director -> directorIds.contains(director.getId()))
                .toList();
    }
}
