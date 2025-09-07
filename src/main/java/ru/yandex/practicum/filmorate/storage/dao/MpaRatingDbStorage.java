package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL__SQL = "SELECT * FROM mpa_ratings ORDER BY id";

    private static final String FIND_BY_ID_SQL = "SELECT * FROM mpa_ratings WHERE id = ?";

    public List<MpaRating> findAll() {
        return jdbcTemplate.query(FIND_ALL__SQL, this::mapRowToMpaRating);
    }

    public Optional<MpaRating> findById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_BY_ID_SQL, this::mapRowToMpaRating, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private MpaRating mapRowToMpaRating(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getInt("id"), rs.getString("name"));
    }
}