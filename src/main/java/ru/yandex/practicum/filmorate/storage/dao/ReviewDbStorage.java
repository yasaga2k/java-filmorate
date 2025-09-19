package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDbStorage  implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        if (film(review.getFilmId()) && user(review.getUserId())) {
            String sqlQuery = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID)" +
                    "values (?, ?, ?, ?)";

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"REVIEW_ID"});
                stmt.setString(1, review.getContent());
                stmt.setBoolean(2, review.getIsPositive());
                stmt.setInt(3, review.getUserId());
                stmt.setInt(4, review.getFilmId());
                return stmt;
            }, keyHolder);
            review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
            return review;
        } else
            throw new ValidationException("Отзыв не добавлен.");
    }

    @Override
    public Optional<Review> update(Review review) {
        String sqlQuery = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ?" +
                " WHERE REVIEW_ID = ?";

        int result = jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        if (result == 0)
            return Optional.empty();
        return findById(review.getReviewId());

    }

    @Override
    public void delete(int id) {
        String sql =
                "DELETE " +
                        "FROM REVIEWS " +
                        "WHERE REVIEW_ID = ?";

        int result = jdbcTemplate.update(sql, id);
        if (result == 1)
            log.info("Удалён отзыв id {}", id);
        else
            throw new NotFoundException("Отзыв для удаления не найден.");
    }

    @Override
    public Optional<Review> findById(int id) {
        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT r.*, " +
                "SUM(CASE WHEN LR.IS_POSITIVE = TRUE THEN 1 WHEN LR.IS_POSITIVE = FALSE THEN -1 ELSE 0 END) AS USE " +
                "FROM REVIEWS AS r " + "LEFT JOIN LIKE_REVIEW as LR on r.REVIEW_ID = LR.REVIEW_ID " +
                " WHERE r.REVIEW_ID = ? GROUP BY r.REVIEW_ID", id);
        if (reviewRows.next()) {
            return Optional.of(reviewRows(reviewRows));
        } else log.info("Фильм с идентификатором {} не найден.", id);
        return Optional.empty();
    }

    @Override
    public List<Review> findAll(int filmId, int count) {
        String where = "";
        if (filmId != 0) where = "WHERE FILM_ID = " + filmId;
        String sql = "SELECT r.*, " +
                "SUM(CASE WHEN LR.IS_POSITIVE = TRUE THEN 1 WHEN LR.IS_POSITIVE = FALSE THEN -1 ELSE 0 END) AS USE " +
                "FROM REVIEWS AS r " +
                "LEFT JOIN LIKE_REVIEW as LR on r.REVIEW_ID = LR.REVIEW_ID " + where +
                " GROUP BY r.REVIEW_ID " + " ORDER BY USE DESC" +
                " LIMIT " + count;
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs));
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return new Review(rs.getInt("REVIEW_ID"),
                rs.getString("CONTENT"),
                rs.getBoolean("IS_POSITIVE"),
                rs.getInt("USER_ID"),
                rs.getInt("FILM_ID"),
                rs.getInt("USE"));
    }

    private Review reviewRows(SqlRowSet rs) {
        return new Review(rs.getInt("REVIEW_ID"),
                rs.getString("CONTENT"),
                rs.getBoolean("IS_POSITIVE"),
                rs.getInt("USER_ID"),
                rs.getInt("FILM_ID"),
                rs.getInt("USE"));
    }

    private boolean film(Integer id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM FILMS " +
                "WHERE FILM_ID = ?", id);
        if (filmRows.next()) {
            return true;
        } else log.info("Фильм с идентификатором {} не найден.", id);
        if (id == 0) {
            throw new ValidationException("У фильма не может быть id = 0.");
        } else
            throw new NotFoundException("Фильм не найден.");
    }

    private boolean user(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * " +
                "FROM USERS " +
                "WHERE USER_ID = ?", id);
        if (userRows.next()) {
            return true;
        } else log.info("Пользователь с идентификатором {} не найден.", id);
        throw new NotFoundException("Пользователь не найден.");
    }

    @Override
    public void createLike(int id, int userId) {
        String sqlQuery = "INSERT " +
                "INTO like_review (review_id, user_id, is_positive) " +
                "VALUES (?, ?, TRUE)";

        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void createDislike(int id, int userId) {
        String sqlQuery = "INSERT " +
                "INTO like_review (review_id, user_id, is_positive) " +
                "VALUES (?, ?, FALSE)";

        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        String sqlQuery = "DELETE " +
                "FROM like_review " +
                "WHERE review_id = ? " +
                "AND user_id = ? " +
                "AND is_positive = TRUE";

        jdbcTemplate.update(sqlQuery, id, userId);
    }

    @Override
    public void deleteDislike(int id, int userId) {
        String sqlQuery = "DELETE " +
                "FROM like_review " +
                "WHERE review_id = ? " +
                "AND user_id = ? " +
                "AND is_positive = FALSE";

        jdbcTemplate.update(sqlQuery, id, userId);
    }
}


