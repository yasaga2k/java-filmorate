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
        // Здесь вы можете добавить валидацию объекта review
        Review updatedReview = reviewService.update(review);
        return ResponseEntity.ok(updatedReview);
    }

    // удаление
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable int id) {
        reviewService.delete(id); // Вызов метода delete в сервисе для удаления отзыва
        return ResponseEntity.noContent().build();
    }


}
