package ru.yandex.practicum.filmorate.storage.dao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Component
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_BY_ID_SQL = "SELECT * FROM reviews WHERE reviewId = ?";

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

    public void delete(int id) {
        String sql = "DELETE FROM reviews WHERE reviewId = ?";
        jdbcTemplate.update(sql, id);
    }


    public Optional<Review> findById(int id) {
        try {
            Review review = jdbcTemplate.queryForObject(FIND_BY_ID_SQL, this::mapRowToReview, id);
            if (review != null) {
                return Optional.of(review);
            }
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setId(rs.getInt("reviewId")); // почемуто @Data не работает
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("isPositive")); // почемуто @Data не работает
        review.setUserId(rs.getInt("userId"));
        review.setFilmId(rs.getInt("filmId"));
        review.setUseful(rs.getInt("useful"));

        return review;
    }

}


