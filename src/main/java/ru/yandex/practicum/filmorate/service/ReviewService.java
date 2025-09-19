package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public Review save(Review review) {
        validateReview(review);
        checkFilmAndUserExistence(review);

        return reviewStorage.save(review);
    }

    public Review update(Review review) {
        validateReview(review);
        checkFilmAndUserExistence(review);

        return reviewStorage.update(review);
    }

    public void delete(int id) {
        Optional<Review> optionalReview = reviewStorage.findById(id);
        if (!optionalReview.isPresent()) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        Review existingReview = optionalReview.get();
        reviewStorage.delete(id);
    }


    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isEmpty()) {
            throw new ValidationException("Содержание отзыва не может быть пустым.");
        }
    }

    private void checkFilmAndUserExistence(Review review) {
        int filmId = review.getFilmId();
        int userId = review.getUserId();

        try {
            filmService.findById(filmId); // Проверяем существование фильма
            userService.findById(userId); // Проверяем существование пользователя
        } catch (NotFoundException e) {
            log.error("Ошибка при  проверке существования фильма или пользователя: {}", e.getMessage());
            throw new ValidationException("При работе с отзывом фильм или пользователь не были найдены.");
        }
    }


    public Optional<Review> findById(int id) {
        Optional<Review> optionalReview = reviewStorage.findById(id);
        optionalReview.ifPresent(System.out::println);
        return optionalReview;
    }

    public List<Review> findByFilmId(Integer filmId, int count) {
        return reviewStorage.findByFilmId(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        updateReviewRating(reviewId, 1); // Увеличиваем рейтинг на 1
    }

    public void removeLike(int reviewId, int userId) {
        updateReviewRating(reviewId, -1); // Уменьшаем рейтинг на 1
    }

    public void addDislike(int reviewId, int userId) {
        updateReviewRating(reviewId, -1); // Уменьшаем рейтинг на 1
    }

    public void removeDislike(int reviewId, int userId) {
        updateReviewRating(reviewId, 1); // Увеличиваем рейтинг на 1
    }

    public void updateReviewRating(int reviewId, int ratingChange) {
        Optional<Review> optionalReview = reviewStorage.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            int newUseful = review.getUseful() + ratingChange;
            if (newUseful >= 0) { // Проверяем, что новое значение не отрицательное
                review.setUseful(newUseful); // Обновляем рейтинг
                reviewStorage.update(review); // Сохраняем изменения в базе данных
            } else {
                throw new IllegalArgumentException("Рейтинг полезности не может быть отрицательным");
            }
        } else {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }
}
