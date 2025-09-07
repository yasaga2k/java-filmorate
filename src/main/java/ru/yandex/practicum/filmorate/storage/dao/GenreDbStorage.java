package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_GENRES_SQL = "SELECT * FROM genres ORDER BY id";

    private static final String FIND_BY_ID_GENRES_SQL = "SELECT * FROM genres WHERE id = ?";

    public List<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES_SQL, this::mapRowToGenre);
    }

    public Optional<Genre> findById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_BY_ID_GENRES_SQL, this::mapRowToGenre, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}