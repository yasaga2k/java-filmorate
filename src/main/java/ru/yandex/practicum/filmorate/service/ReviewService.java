package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.ReviewCreateDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewGetDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvents;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.FeedEventsDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.mappers.ReviewRowMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {
    ReviewDbStorage reviewStorage;
    FilmDbStorage filmDbStorage;
    UserDbStorage userDbStorage;
    FeedEventsDbStorage feedEventsDbStorage;
    ReviewRowMapper reviewRowMapper;

    public ReviewGetDto createReview(ReviewCreateDto reviewCreateDto) {
        validateReview(reviewCreateDto.getFilmId(), reviewCreateDto.getUserId());

        Review review = Review.builder()
                .content(reviewCreateDto.getContent())
                .isPositive(reviewCreateDto.getIsPositive())
                .filmId(reviewCreateDto.getFilmId())
                .userId(reviewCreateDto.getUserId())
                .useful(reviewCreateDto.getUseful())
                .build();

        Review savedReview = reviewStorage.createReview(review);

        FeedEvents feedEvents = new FeedEvents(
                1,
                System.currentTimeMillis(),
                savedReview.getUserId(),
                "REVIEW",
                "ADD",
                savedReview.getReviewId().intValue());
        feedEventsDbStorage.save(feedEvents);

        return reviewRowMapper.toReviewGetDto(savedReview);
    }

    public ReviewGetDto updateReview(ReviewUpdateDto reviewUpdateDto) {
        validateReview(reviewUpdateDto.getFilmId(), reviewUpdateDto.getUserId());

        Review review = Review.builder()
                .reviewId(reviewUpdateDto.getReviewId())
                .content(reviewUpdateDto.getContent())
                .isPositive(reviewUpdateDto.getIsPositive())
                .filmId(reviewUpdateDto.getFilmId())
                .userId(reviewUpdateDto.getUserId())
                .useful(reviewUpdateDto.getUseful())
                .build();

        Review reviewUpdated = reviewStorage.updateReview(review);
        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                reviewUpdated.getUserId(),
                "REVIEW",
                "UPDATE",
                reviewUpdated.getReviewId().intValue()));

        return reviewRowMapper.toReviewGetDto(reviewUpdated);
    }

    public ReviewGetDto getReviewById(int id) {
        Review review = reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден."));
        return reviewRowMapper.toReviewGetDto(review);
    }

    public List<ReviewGetDto> getReviewByFilmId(int filmId, int count) {
        List<Review> reviews = reviewStorage.findReviewsByFilm(filmId, count);
        return reviews.stream()
                .map(reviewRowMapper::toReviewGetDto)
                .collect(Collectors.toList());
    }

    public void deleteReview(int id) {
        Review review = reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден."));

        feedEventsDbStorage.save(new FeedEvents(
                1,
                System.currentTimeMillis(),
                review.getUserId(),
                "REVIEW",
                "REMOVE",
                review.getReviewId().intValue()));
        reviewStorage.deleteReview(id);
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