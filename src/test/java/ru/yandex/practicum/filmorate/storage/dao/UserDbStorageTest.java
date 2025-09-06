package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void testFindUserById() {
        // Given
        User user = createTestUser();
        User createdUser = userStorage.create(user);

        // When
        Optional<User> userOptional = userStorage.findById(createdUser.getId());

        // Then
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", createdUser.getId())
                );
    }

    @Test
    void testFindAllUsers() {
        // Given
        User user1 = createTestUser();
        User user2 = createTestUser();
        user2.setEmail("test2@mail.com");
        user2.setLogin("testuser2");

        userStorage.create(user1);
        userStorage.create(user2);

        // When
        List<User> users = userStorage.findAll();

        // Then
        assertThat(users).hasSize(2);
    }

    @Test
    void testCreateUser() {
        // Given
        User user = createTestUser();

        // When
        User createdUser = userStorage.create(user);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
    }

    @Test
    void testUpdateUser() {
        // Given
        User user = createTestUser();
        User createdUser = userStorage.create(user);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.com");

        // When
        User updatedUser = userStorage.update(createdUser);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.com");
    }

    @Test
    void testDeleteUser() {
        // Given
        User user = createTestUser();
        User createdUser = userStorage.create(user);

        // When
        userStorage.delete(createdUser.getId());

        // Then
        Optional<User> deletedUser = userStorage.findById(createdUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}