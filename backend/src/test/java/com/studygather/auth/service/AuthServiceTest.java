package com.studygather.auth.service;

import com.studygather.auth.exception.InvalidCredentialsException;
import com.studygather.user.dto.request.LoginRequest;
import com.studygather.user.dto.request.SignUpRequest;
import com.studygather.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Test
    void loginSucceedsWithCorrectCredentials() {
        userService.signUp(new SignUpRequest(
                "login@example.com",
                "password123",
                "login-user"
        ));
        LoginRequest request = new LoginRequest("login@example.com", "password123");

        assertDoesNotThrow(() -> authService.login(request));
    }

    @Test
    void loginRejectsUnknownEmail() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void loginRejectsIncorrectPassword() {
        userService.signUp(new SignUpRequest(
                "wrong-password@example.com",
                "password123",
                "wrong-password-user"
        ));
        LoginRequest request = new LoginRequest(
                "wrong-password@example.com",
                "different-password"
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }
}
