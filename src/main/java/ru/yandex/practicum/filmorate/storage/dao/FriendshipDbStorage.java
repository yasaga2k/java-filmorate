package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void add(Friendship friendship) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, friendship.userId(), friendship.friendId(), "PENDING");
    }

    @Override
    public void update(Friendship friendship) {
        String sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, "CONFIRMED", friendship.userId(), friendship.friendId());
    }

    @Override
    public void delete(Friendship friendship) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, friendship.userId(), friendship.friendId());
    }

    @Override
    public List<Friendship> getFriendshipByUserId(int userId) {
        String sql = "SELECT * FROM friendships WHERE user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToFriendship, userId);
    }

    private Friendship mapRowToFriendship(ResultSet rs, int rowNum) throws SQLException {
        return new Friendship(
                rs.getInt("user_id"),
                rs.getInt("friend_id"),
                "CONFIRMED".equals(rs.getString("status"))
        );
    }
}