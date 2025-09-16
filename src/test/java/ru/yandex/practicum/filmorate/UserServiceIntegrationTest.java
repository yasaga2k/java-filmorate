package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testlogin");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUserShouldReturnUserWithId() {
        User createdUser = userService.create(testUser);

        assertNotNull(createdUser.getId());
        assertTrue(createdUser.getId() > 0);
        assertEquals("testlogin", createdUser.getLogin());
    }

    @Test
    void createUserWithoutNameShouldUseLogin() {
        testUser.setName(null);

        User createdUser = userService.create(testUser);

        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void findAllShouldReturnCreatedUsers() {
        userService.create(testUser);

        List<User> users = userService.findAll();

        assertFalse(users.isEmpty());
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
    void addFriendShouldAddFriendship() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@mail.com");
        user2.setLogin("friend");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User friend = userService.create(user2);

        userService.addFriend(user1.getId(), friend.getId());

        List<User> friends = userService.getFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals("friend", friends.get(0).getLogin());
    }

    @Test
    void removeFriendShouldRemoveFriendship() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("friend@mail.com");
        user2.setLogin("friend");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User friend = userService.create(user2);

        userService.addFriend(user1.getId(), friend.getId());
        userService.removeFriend(user1.getId(), friend.getId());

        List<User> friends = userService.getFriends(user1.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriendsShouldReturnCommonFriends() {
        User user1 = userService.create(testUser);

        User user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User createdUser2 = userService.create(user2);

        User commonFriend = new User();
        commonFriend.setEmail("common@mail.com");
        commonFriend.setLogin("common");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        User createdCommon = userService.create(commonFriend);

        userService.addFriend(user1.getId(), createdCommon.getId());
        userService.addFriend(createdUser2.getId(), createdCommon.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), createdUser2.getId());
        assertEquals(1, commonFriends.size());
        assertEquals("common", commonFriends.get(0).getLogin());
    }
}