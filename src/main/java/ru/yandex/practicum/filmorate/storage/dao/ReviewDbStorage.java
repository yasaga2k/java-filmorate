package ru.yandex.practicum.filmorate.storage.dao;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewDbStorage extends BaseRepository<Review> {

    static String SELECT_REVIEWS = "SELECT * FROM film_reviews ";
    static String CREATE_REVIEW_SQL = "INSERT INTO film_reviews (film_id, user_id, content, is_positive, useful) VALUES (?, ?, ?, ?, ?)";
    static String UPDATE_REVIEW_SQL = "UPDATE film_reviews SET content = ?, is_positive = ? WHERE id = ?";
    static String DELETE_REVIEW_SQL = "DELETE FROM film_reviews WHERE id = ?";
    static String FIND_REVIEWS_BY_FILM_SQL = "SELECT * FROM film_reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    static String FIND_ALL_REVIEWS_SQL = "SELECT * FROM film_reviews ORDER BY useful DESC LIMIT ?";
    static String FIND_REVIEW_BY_ID_SQL = "SELECT * FROM film_reviews WHERE id = ?";
    static String CHECK_LIKE_EXISTS_SQL = "SELECT COUNT(*) AS count FROM reviews_likes WHERE review_id = ? AND user_id = ?";
    static String UPDATE_LIKE_SQL = "UPDATE reviews_likes SET is_positive = ? WHERE review_id = ? AND user_id = ?";
    static String INSERT_LIKE_SQL = "INSERT INTO reviews_likes (review_id, user_id, is_positive) VALUES (?, ?, ?)";
    static String DELETE_LIKE_SQL = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ? AND is_positive = ?";
    static String UPDATE_USEFUL_SQL = "UPDATE film_reviews SET useful = useful + ? WHERE id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    public Review createReview(Review review) {
        long id = insert(CREATE_REVIEW_SQL,
                review.getFilmId(), review.getUserId(), review.getContent(), review.isPositive(), review.getUseful());
        review.setReviewId(id);
        return review;
    }

    public Review updateReview(Review review) {
        long id = review.getReviewId();
        if (jdbc.update(UPDATE_REVIEW_SQL,
                review.getContent(),
                review.isPositive(),
                id) < 1) {
            throw new NotFoundException("Отзыв с id " + review.getReviewId() + " не найден.");
        }
        return findById(id).get();
    }

    public void deleteReview(int id) {
        jdbc.update(DELETE_REVIEW_SQL, id);
    }

    public List<Review> findReviewsByFilm(int filmId, int count) {
        if (filmId != -1) {
            return jdbc.query(FIND_REVIEWS_BY_FILM_SQL, mapper, filmId, count);
        }
        return jdbc.query(FIND_ALL_REVIEWS_SQL, mapper, count);
    }

    public Optional<Review> findById(long id) {
        String sql = SELECT_REVIEWS + "WHERE id = ?";
        return findOne(sql, id);
    }

    public boolean likeExistsById(int reviewId, int userId) {
        Integer count = jdbc.queryForObject(CHECK_LIKE_EXISTS_SQL, Integer.class, reviewId, userId);
        return count != null && count > 0;
    }

    public void addLike(int reviewId, int userId, boolean positive) {
        String sql;
        int amount;

        if (likeExistsById(reviewId, userId)) {
            sql = UPDATE_LIKE_SQL;
            amount = positive ? 2 : -2;
            update(sql, positive, reviewId, userId);
        } else {
            sql = INSERT_LIKE_SQL;
            amount = positive ? 1 : -1;
            update(sql, reviewId, userId, positive);
        }

        updateUseful(reviewId, amount);
    }

    public void removeLike(int reviewId, int userId, boolean positive) {
        if (likeExistsById(reviewId, userId)) {
            jdbc.update(DELETE_LIKE_SQL, reviewId, userId, positive);
            int amount = positive ? -1 : 1;
            updateUseful(reviewId, amount);
        }
    }

    private void updateUseful(int reviewId, int amount) {
        jdbc.update(UPDATE_USEFUL_SQL, amount, reviewId);
    }
}