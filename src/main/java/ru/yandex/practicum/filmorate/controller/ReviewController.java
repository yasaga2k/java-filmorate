package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // создание отзыва
    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody Review review) {
        log.info("Создание нового отзыва: {}", review);
        Review savedReview = reviewService.save(review);
        return ResponseEntity.ok(savedReview);
    }

    // обновление отзыва
    @PutMapping("/reviews")
    public ResponseEntity<Review> updateReview(@RequestBody Review review) {
        Review updatedReview = reviewService.update(review);
        return ResponseEntity.ok(updatedReview);
    }

    // удаление отзыва
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

    // Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10.
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviewsByFilmId(@RequestParam(required = false) Integer filmId,
                                                           @RequestParam(defaultValue = "10") int count) {
        List<Review> reviews = reviewService.findByFilmId(filmId, count); // Думаю можно через filmService
        return ResponseEntity.ok(reviews);
    }

    // добавление лайка
    @PutMapping("/reviews/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    // удаление лайка
    @DeleteMapping("/reviews/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    // добавление дизлайка
    @PutMapping("/reviews/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
        return ResponseEntity.ok().build();
    }

    //Удаление
    @DeleteMapping("/reviews/{id}/dislike/{userId}")
    public ResponseEntity<Void> removeDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeDislike(id, userId);
        return ResponseEntity.ok().build();
    }
}
