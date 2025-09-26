package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
@Primary
@RequiredArgsConstructor
public class DirectorDbStorage {
    private static final String FIND_ALL_SQL = "SELECT * FROM directors";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM directors WHERE id = ?";
    private static final String CREATE_SQL = "INSERT INTO directors (name) VALUES (?)";
    private static final String DELETE_SQL = "DELETE FROM directors WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE directors SET name = ? WHERE id = ?";
    private final JdbcTemplate jdbcTemplate;

    public List<Director> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, this::mapRowToDirector);
    }

    public Optional<Director> findById(int id) {
        try {
            Director director = jdbcTemplate.queryForObject(FIND_BY_ID_SQL, this::mapRowToDirector, id);
            if (director != null) {
                return Optional.of(director);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection
                    .prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    public void deleteById(int id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    public Director update(Director request) {
        Director updateDirector;
        try {
            updateDirector = jdbcTemplate.queryForObject(FIND_BY_ID_SQL, new Object[]{request.getId()},
                    (rs, rowNum) -> mapRowToDirector(rs, rowNum));
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Режиссёр не найден");
        }

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL);
            stmt.setString(1, updateDirector.getName());
            stmt.setInt(2, updateDirector.getId());
            return stmt;
        });

        return updateDirector;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));
        return director;
    }

}
