package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // создание
    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        log.info("Создание нового отзыва: {}", review);
        Review savedReview = reviewService.save(review);
        return ResponseEntity.ok(savedReview);
    }

    // обновление
    @PutMapping("/reviews")
    public ResponseEntity<Review> updateReview(@RequestBody Review review) {
        Review updatedReview = reviewService.update(review);
        return ResponseEntity.ok(updatedReview);
    }

    // удаление
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable int id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
     // поиск по id
    @GetMapping("/reviews/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable int id) {
        Review review = reviewService.findById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
    }

}
