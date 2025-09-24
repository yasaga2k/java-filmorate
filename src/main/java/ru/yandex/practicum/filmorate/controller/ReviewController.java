package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.dto.ReviewCreateDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewGetDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,
        makeFinal = true)
public class ReviewController {
    ReviewService reviewService;

    @PostMapping()
    public ResponseEntity<ReviewGetDto> createReview(@Valid @RequestBody ReviewCreateDto reviewCreateDto) {
        try {
            Review review = Review.builder()
                    .content(reviewCreateDto.getContent())
                    .isPositive(reviewCreateDto.getIsPositive())
                    .filmId(reviewCreateDto.getFilmId())
                    .userId(reviewCreateDto.getUserId())
                    .useful(reviewCreateDto.getUseful())
                    .build();
            Review createdReview = reviewService.createReview(review);
            return ResponseEntity.ok(toReviewGetDto(createdReview));
        } catch (ValidationException e) {
            // Возврат кода 400 в случае ошибки валидации
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NotFoundException e) {
            // Возврат кода 404 в случае, если ресурс не найден
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Возврат кода 500 в случае внутренней ошибки сервера
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping()
    public ResponseEntity<ReviewGetDto> updateReview(@Valid @RequestBody ReviewUpdateDto reviewUpdateDto) {
        Review review = Review.builder()
                .reviewId(reviewUpdateDto.getReviewId())
                .content(reviewUpdateDto.getContent())
                .isPositive(reviewUpdateDto.getIsPositive())
                .filmId(reviewUpdateDto.getFilmId())
                .userId(reviewUpdateDto.getUserId())
                .useful(reviewUpdateDto.getUseful())
                .build();
        Review updatedReview = reviewService.updateReview(review);
        return ResponseEntity.ok(toReviewGetDto(updatedReview));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewGetDto> getReviewById(@PathVariable int id) {
        Review review = reviewService.getReviewById(id);
        return ResponseEntity.ok(toReviewGetDto(review));
    }

    @GetMapping()
    public ResponseEntity<List<ReviewGetDto>> getReviews(
            @RequestParam(name = "filmId", defaultValue = "-1") int filmId,
            @RequestParam(name = "count", defaultValue = "10") int count) {
        List<Review> reviews = reviewService.getReviewByFilmId(filmId, count);

        List<ReviewGetDto> reviewsGetDto = reviews.stream()
                .map(this::toReviewGetDto)
                .toList();

        return ResponseEntity.ok(reviewsGetDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("");
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.likeReview(id, userId, true);
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId, true);
        return ResponseEntity.ok("");
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<?> addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.likeReview(id, userId, false);
        return ResponseEntity.ok("");
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<?> deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId, false);
        return ResponseEntity.noContent().build();
    }

    public ReviewGetDto toReviewGetDto(Review review) {
        return ReviewGetDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .filmId(review.getFilmId())
                .userId(review.getUserId())
                .isPositive(review.isPositive())
                .useful(review.getUseful())
                .build();
    }
}
