package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,
        makeFinal = true)
public class ReviewService {
    ReviewDbStorage reviewStorage;
    FilmDbStorage filmDbStorage;
    UserDbStorage userDbStorage;

    public Review getReviewById(int id) {
        return reviewStorage.findById(id).orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден."));
    }

    public Review updateReview(Review review) {
        validateReview(review.getFilmId(), review.getUserId());
        return reviewStorage.updateReview(review);
    }

    public Review createReview(Review review) {
        validateReview(review.getFilmId(), review.getUserId());
        return reviewStorage.createReview(review);
    }

    public void deleteReview(int id) {
        Review review = getReviewById(id); // получаем, чтобы узнать userId
        reviewStorage.deleteReview(id);
    }

    public List<Review> getReviewByFilmId(int filmId, int count) {
        return reviewStorage.findReviewsByFilm(filmId, count);
    }

    public void likeReview(int reviewId, int userId, boolean isPositive) {
        validateLike(reviewId, userId);
        reviewStorage.addLike(reviewId, userId, isPositive);
    }

    public void removeLike(int reviewId, int userId, boolean isPositive) {
        validateLike(reviewId, userId);
        reviewStorage.removeLike(reviewId, userId, isPositive);
    }

    private void validateLike(int reviewId, int userId) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден.");
        }
        if (userDbStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
    }

    private void validateReview(int filmId, int userId) {
        if (filmDbStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }
        if (userDbStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
    }
}