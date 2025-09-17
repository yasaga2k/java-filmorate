package ru.yandex.practicum.filmorate.storage.dao;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Component
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review save(Review review) {
        String sql = "INSERT INTO reviews (content, isPositive, userId, filmId, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.isPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);

        review.setFilmId(keyHolder.getKey().intValue());
        return review;
    }

    public Review update(Review review) {
        String sql = "UPDATE reviews " +
                "SET content = ?, " +
                "    isPositive = ?, " +
                "    userId = ?, " +
                "    filmId = ?, " +
                "    useful = ? " +
                "WHERE reviewId = ?";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.isPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful(),
                review.getReviewId());

        return review;
    }
}


