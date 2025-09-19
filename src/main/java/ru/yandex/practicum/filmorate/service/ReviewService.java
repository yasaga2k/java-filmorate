package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewDbStorage reviewStorage;

    public Review save(Review review) {
        Review reviewCreated = reviewStorage.create(review);
        return reviewCreated;
    }

    public Review update(Review review) {
        Optional<Review> reviewUpdated = reviewStorage.update(review);
        return reviewUpdated.orElse(null);
    }

    public void delete(Integer id) {
        reviewStorage.delete(id);
    }

    public Review findById(Integer id) {
        return reviewStorage.findById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден."));
    }

    public List<Review> findAll(Integer filmId, Integer count) {
        return reviewStorage.findAll(filmId, count);
    }

    public void createLike(Integer id, Integer userId) {
        reviewStorage.createLike(id, userId);
    }

    public void createDislike(Integer id, Integer userId) {
        reviewStorage.createDislike(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        reviewStorage.deleteLike(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        reviewStorage.deleteDislike(id, userId);
    }
}
