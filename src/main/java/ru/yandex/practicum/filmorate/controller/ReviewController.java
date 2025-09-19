package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // создание отзыва
    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        Review savedReview = reviewService.save(review);
        return ResponseEntity.ok(savedReview);
    }

    // обновление отзыва
    @PutMapping
    public ResponseEntity<Review> updateReview(@Valid @RequestBody Review review) {
        Review updatedReview = reviewService.update(review);
        return ResponseEntity.ok(updatedReview);
    }

    // удаление отзыва
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // поиск по id
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Integer id) {
        Optional<Review> optionalReview = reviewService.findById(id);
        if (optionalReview.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalReview.get());
    }

    @GetMapping()
    public ResponseEntity<List<Review>> getReviewsByFilmId(
            @RequestParam(defaultValue = "0", required = false) @Positive Integer filmId,
            @RequestParam(defaultValue = "10", required = false) @Positive Integer count) {
        List<Review> reviews = reviewService.findAll(filmId, count);
        return ResponseEntity.ok(reviews);
    }

    // добавление лайка
    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.createLike(id, userId);
        return ResponseEntity.ok().build();
    }

    // удаление лайка
    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    // добавление дизлайка
    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.createDislike(id, userId);
        return ResponseEntity.ok().build();
    }

    // Удаление дизлайка
    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> removeDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.deleteDislike(id, userId);
        return ResponseEntity.ok().build();
    }
}

