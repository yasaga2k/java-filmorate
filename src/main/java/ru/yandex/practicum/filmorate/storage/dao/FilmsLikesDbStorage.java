package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FilmsLikesDbStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_LIKE_SQL = "INSERT INTO films_likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_SQL = "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_LIKES_BY_FILM_SQL = "SELECT user_id FROM films_likes WHERE film_id = ?";

    public void addLike(int filmId, int userId) {
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    public Set<Integer> getLikesByFilmId(int filmId) {
        return Set.copyOf(jdbcTemplate.query(GET_LIKES_BY_FILM_SQL,
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId));
    }
}