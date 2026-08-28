package com.studygather.user.repository;

import com.studygather.support.MySqlTestcontainersConfiguration;
import com.studygather.user.entity.User;
import com.studygather.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("testcontainers")
@Import(MySqlTestcontainersConfiguration.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByEmail() {
        User user = User.create(
                "user@example.com",
                "encoded-password",
                "study-user"
        );

        userRepository.saveAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("user@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("study-user", foundUser.get().getNickname());
        assertEquals(UserRole.USER, foundUser.get().getRole());
        assertNotNull(foundUser.get().getCreatedAt());
        assertNotNull(foundUser.get().getUpdatedAt());
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(User.create(
                "duplicate@example.com",
                "encoded-password",
                "first-user"
        ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(User.create(
                        "duplicate@example.com",
                        "encoded-password",
                        "second-user"
                ))
        );
    }
}
