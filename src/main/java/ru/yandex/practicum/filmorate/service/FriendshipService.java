package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.List;

public class FriendshipService {

    private final FriendshipStorage friendshipStorage;

    public FriendshipService(FriendshipStorage friendshipStorage) {
        this.friendshipStorage = friendshipStorage;
    }

    public void addFriend(int userId, int friendId) {
        Friendship friendship = new Friendship(userId, friendId, true);
        friendshipStorage.add(friendship);
    }

    public void deleteFriend(int userId, int friendId) {
        Friendship friendship = new Friendship(userId, friendId, false); // status doesn't matter here
        friendshipStorage.delete(friendship);
    }

    public List<Friendship> getFriends(int userId) {
        return friendshipStorage.getFriendshipByUserId(userId);
    }
}
