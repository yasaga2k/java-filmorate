package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.dto.ReviewCreateDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewGetDto;
import ru.yandex.practicum.filmorate.controller.dto.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {
    ReviewService reviewService;

    @PostMapping()
    public ResponseEntity<ReviewGetDto> createReview(@Valid @RequestBody ReviewCreateDto reviewCreateDto) {
        ReviewGetDto createdReview = reviewService.createReview(reviewCreateDto);
        return ResponseEntity.ok(createdReview);
    }

    @PutMapping()
    public ResponseEntity<ReviewGetDto> updateReview(@Valid @RequestBody ReviewUpdateDto reviewUpdateDto) {
        ReviewGetDto updatedReview = reviewService.updateReview(reviewUpdateDto);
        return ResponseEntity.ok(updatedReview);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewGetDto> getReviewById(@PathVariable int id) {
        ReviewGetDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @GetMapping()
    public ResponseEntity<List<ReviewGetDto>> getReviews(
            @RequestParam(name = "filmId", defaultValue = "-1") int filmId,
            @RequestParam(name = "count", defaultValue = "10") int count) {
        List<ReviewGetDto> reviews = reviewService.getReviewByFilmId(filmId, count);
        return ResponseEntity.ok(reviews);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.likeReview(id, userId, true);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.likeReview(id, userId, false);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId, false);
        return ResponseEntity.noContent().build();
    }
}