package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvents;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.FeedEventsDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,
        makeFinal = true)
public class ReviewService {
    ReviewDbStorage reviewStorage;
    FilmDbStorage filmDbStorage;
    UserDbStorage userDbStorage;
    FeedEventsDbStorage feedEventsDbStorage;

    public Review getReviewById(int id) {
        return reviewStorage.findById(id).orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден."));
    }

    public Review updateReview(Review review) {
        validateReview(review.getFilmId(), review.getUserId());
        Review reviewUpdated = reviewStorage.updateReview(review);
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                reviewUpdated.getUserId(),
                "REVIEW",
                "UPDATE",
                reviewUpdated.getReviewId().intValue()));
        return reviewUpdated;
    }

    public Review createReview(Review review) {
        validateReview(review.getFilmId(), review.getUserId());

        Review savedReview = reviewStorage.createReview(review); // now it has an ID

        FeedEvents feedEvents = new FeedEvents(
                1,
                System.currentTimeMillis(),
                savedReview.getUserId(),
                "REVIEW",
                "ADD",
                savedReview.getReviewId().intValue());

        feedEventsDbStorage.save(feedEvents);

        return savedReview;
    }


    public void deleteReview(int id) {
        Review review = getReviewById(id); // получаем, чтобы узнать userId
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                review.getUserId(),
                "REVIEW",
                "REMOVE",
                review.getReviewId().intValue()));
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