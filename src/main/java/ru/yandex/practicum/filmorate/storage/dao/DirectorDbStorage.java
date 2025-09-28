package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
@Primary
public class DirectorDbStorage extends BaseRepository<Director> {
    private static final String FIND_ALL_SQL = "SELECT * FROM directors";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM directors WHERE id = ?";
    private static final String CREATE_SQL = "INSERT INTO directors (name) VALUES (?)";
    private static final String DELETE_SQL = "DELETE FROM directors WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE directors SET name = ? WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getInt("id"));
            director.setName(rs.getString("name"));
            return director;
        });
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_SQL);
    }

    public Optional<Director> findById(int id) {
        return findOne(FIND_BY_ID_SQL, id);
    }

    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement stmt = connection
                    .prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    public void deleteById(int id) {
        update(DELETE_SQL, id);
    }

    public Director update(Director director) {
        update(UPDATE_SQL, director.getName(), director.getId());
        return director;
    }
}
