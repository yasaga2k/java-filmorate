package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_SQL = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_SQL = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDSHIP_BY_USER_SQL = "SELECT * FROM friendships WHERE user_id = ?";
    private static final String FIND_COMMON_FRIENDS_IDS_SQL = """
            SELECT f1.friend_id 
            FROM friendships f1
            JOIN friendships f2 ON f1.friend_id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            """;

    @Override
    public void add(Friendship friendship) {
        String status = friendship.confirmed() ? "CONFIRMED" : "PENDING";
        jdbcTemplate.update(ADD_SQL, friendship.userId(), friendship.friendId(), status);
    }

    @Override
    public void update(Friendship friendship) {
        jdbcTemplate.update(UPDATE_SQL, "CONFIRMED", friendship.userId(), friendship.friendId());
    }

    @Override
    public void delete(Friendship friendship) {
        jdbcTemplate.update(DELETE_SQL, friendship.userId(), friendship.friendId());
    }

    @Override
    public List<Friendship> getFriendshipByUserId(int userId) {
        return jdbcTemplate.query(GET_FRIENDSHIP_BY_USER_SQL, this::mapRowToFriendship, userId);
    }

    @Override
    public List<Integer> findCommonFriends(int userId1, int userId2) {
        return jdbcTemplate.queryForList(FIND_COMMON_FRIENDS_IDS_SQL, Integer.class, userId1, userId2);
    }

    private Friendship mapRowToFriendship(ResultSet rs, int rowNum) throws SQLException {
        return new Friendship(
                rs.getInt("user_id"),
                rs.getInt("friend_id"),
                "CONFIRMED".equals(rs.getString("status"))
        );
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }
}