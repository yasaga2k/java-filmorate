package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmsLikesDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserService userService;

    @Mock
    private UserStorage userStorage;

    @Mock
    private FriendshipDbStorage friendshipDbStorage;

    @Mock
    private FriendshipStorage friendshipStorage;
    
    @Mock
    private FilmsLikesDbStorage filmsLikesDbStorage;
    
    @Mock
    private FilmStorage filmStorage;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userStorage, friendshipDbStorage, friendshipStorage, filmsLikesDbStorage, filmStorage);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@mail.com");
        testUser.setLogin("testlogin");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void createUserShouldReturnUserWithId() {
        when(userStorage.create(any(User.class))).thenReturn(testUser);

        User createdUser = userService.create(testUser);

        assertNotNull(createdUser.getId());
        assertEquals("testlogin", createdUser.getLogin());
        verify(userStorage).create(testUser);
    }

    @Test
    void createUserWithNameShouldUseProvidedName() {
        testUser.setName("Test Name");
        when(userStorage.create(any(User.class))).thenReturn(testUser);

        User createdUser = userService.create(testUser);

        assertEquals("Test Name", createdUser.getName());
        verify(userStorage).create(testUser);
    }

    @Test
    void createUserWithoutNameShouldUseLogin() {
        testUser.setName(null);
        when(userStorage.create(any(User.class))).thenReturn(testUser);

        User createdUser = userService.create(testUser);

        assertEquals("testlogin", createdUser.getName());
        verify(userStorage).create(testUser);
    }

    @Test
    void findAllShouldReturnCreatedUsers() {
        when(userStorage.findAll()).thenReturn(List.of(testUser));

        List<User> users = userService.findAll();

        assertEquals(1, users.size());
        assertEquals("testlogin", users.get(0).getLogin());
        verify(userStorage).findAll();
    }

    @Test
    void findByIdWithExistingIdShouldReturnUser() {
        when(userStorage.findById(1)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findById(1);

        assertEquals(1, foundUser.getId());
        assertEquals("testlogin", foundUser.getLogin());
        verify(userStorage).findById(1);
    }

    @Test
    void findByIdWithNonExistingIdShouldThrowException() {
        when(userStorage.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(999));
        verify(userStorage).findById(999);
    }

    @Test
    void addFriendShouldAddFriendship() {
        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@mail.com");
        friend.setLogin("friend");

        when(userStorage.findById(1)).thenReturn(Optional.of(testUser));
        when(userStorage.findById(2)).thenReturn(Optional.of(friend));

        userService.addFriend(1, 2);

        verify(friendshipDbStorage).add(any(Friendship.class));
        verify(userStorage, times(2)).findById(anyInt());
    }

    @Test
    void removeFriendShouldRemoveFriendship() {
        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@mail.com");
        friend.setLogin("friend");

        when(userStorage.findById(1)).thenReturn(Optional.of(testUser));
        when(userStorage.findById(2)).thenReturn(Optional.of(friend));

        userService.removeFriend(1, 2);

        verify(friendshipDbStorage).delete(any(Friendship.class));
        verify(userStorage, times(2)).findById(anyInt());
    }

    @Test
    void getFriendsShouldReturnUserFriends() {
        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@mail.com");
        friend.setLogin("friend");

        Friendship friendship = new Friendship(1, 2, false);

        when(userStorage.findById(1)).thenReturn(Optional.of(testUser));
        when(friendshipDbStorage.getFriendshipByUserId(1)).thenReturn(List.of(friendship));
        when(userStorage.findById(2)).thenReturn(Optional.of(friend));

        List<User> friends = userService.getFriends(1);

        assertEquals(1, friends.size());
        assertEquals("friend", friends.get(0).getLogin());
        verify(userStorage, times(2)).findById(anyInt());
        verify(friendshipDbStorage).getFriendshipByUserId(1);
    }

    @Test
    void getCommonFriendsShouldReturnCommonFriends() {
        when(userStorage.findById(1)).thenReturn(Optional.of(new User()));
        when(userStorage.findById(2)).thenReturn(Optional.of(new User()));

        when(friendshipDbStorage.getFriendshipByUserId(1)).thenReturn(List.of());
        when(friendshipDbStorage.getFriendshipByUserId(2)).thenReturn(List.of());

        List<User> result = userService.getCommonFriends(1, 2);

        assertEquals(0, result.size());
    }
}