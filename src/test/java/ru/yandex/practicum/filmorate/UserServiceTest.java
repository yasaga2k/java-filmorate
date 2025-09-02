package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());

        testUser = new User();
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testlogin");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUserShouldReturnUserWithId() {
        User createdUser = userService.create(testUser);

        assertNotNull(createdUser.getId());
        assertEquals("testlogin", createdUser.getLogin());
        assertEquals("testlogin", createdUser.getName()); // Должен использовать login как name
    }

    @Test
    void createUserWithNameShouldUseProvidedName() {
        testUser.setName("Test Name");
        User createdUser = userService.create(testUser);

        assertEquals("Test Name", createdUser.getName());
    }

    @Test
    void findAllShouldReturnCreatedUsers() {
        userService.create(testUser);
        List<User> users = userService.findAll();

        assertEquals(1, users.size());
        assertEquals("testlogin", users.get(0).getLogin());
    }

    @Test
    void findByIdWithExistingIdShouldReturnUser() {
        User createdUser = userService.create(testUser);
        User foundUser = userService.findById(createdUser.getId());

        assertEquals(createdUser.getId(), foundUser.getId());
        assertEquals("testlogin", foundUser.getLogin());
    }

    @Test
    void findByIdWithNonExistingIdShouldThrowException() {
        assertThrows(NotFoundException.class, () -> userService.findById(999));
    }

    @Test
    void addFriendShouldAddFriendsToBothUsers() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@mail.com");
        user2.setLogin("friend");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());

        User updatedUser1 = userService.findById(user1.getId());
        User updatedUser2 = userService.findById(createdUser2.getId());

        assertTrue(updatedUser1.getFriends().contains(createdUser2.getId()));
        assertTrue(updatedUser2.getFriends().contains(user1.getId()));
    }

    @Test
    void removeFriendShouldRemoveFriendsFromBothUsers() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@mail.com");
        user2.setLogin("friend");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());
        userService.removeFriend(user1.getId(), createdUser2.getId());

        User updatedUser1 = userService.findById(user1.getId());
        User updatedUser2 = userService.findById(createdUser2.getId());

        assertFalse(updatedUser1.getFriends().contains(createdUser2.getId()));
        assertFalse(updatedUser2.getFriends().contains(user1.getId()));
    }

    @Test
    void getFriendsShouldReturnUserFriends() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@mail.com");
        user2.setLogin("friend");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        userService.addFriend(user1.getId(), createdUser2.getId());
        List<User> friends = userService.getFriends(user1.getId());

        assertEquals(1, friends.size());
        assertEquals("friend", friends.get(0).getLogin());
    }

    @Test
    void getCommonFriendsShouldReturnCommonFriends() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User createdUser2 = userService.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@mail.com");
        commonFriend.setLogin("common");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        User createdCommonFriend = userService.create(commonFriend);

        // Добавляем общего друга обоим пользователям
        userService.addFriend(user1.getId(), createdCommonFriend.getId());
        userService.addFriend(createdUser2.getId(), createdCommonFriend.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), createdUser2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals("common", commonFriends.get(0).getLogin());
    }
}