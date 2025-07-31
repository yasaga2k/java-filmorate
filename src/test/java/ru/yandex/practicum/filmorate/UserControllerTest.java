package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldCreateUser() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1991, 11, 21));

        User createdUser = userController.createUser(user);

        assertEquals(1, createdUser.getId());
        assertEquals(1, userController.findAll().size());
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 12, 28));

        User createdUser = userController.createUser(user);

        assertEquals("login", createdUser.getName());
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.of(1997, 12, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 28));
        userController.createUser(user);

        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated Name");
        user.setBirthday(LocalDate.of(1996, 4, 4));

        User result = userController.updateUser(updatedUser);

        assertEquals("updated@example.com", result.getEmail());
        assertEquals("updatedLogin", result.getLogin());
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentUser() {
        User user = new User();
        user.setId(999);
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1999, 7, 28));

        assertThrows(ValidationException.class, () -> userController.updateUser(user));
    }
}