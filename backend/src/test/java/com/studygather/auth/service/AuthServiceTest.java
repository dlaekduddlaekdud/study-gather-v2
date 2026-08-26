package com.studygather.auth.service;

import com.studygather.auth.exception.InvalidCredentialsException;
import com.studygather.auth.jwt.JwtTokenProvider;
import com.studygather.user.dto.request.LoginRequest;
import com.studygather.user.dto.request.SignUpRequest;
import com.studygather.user.dto.response.LoginResponse;
import com.studygather.user.dto.response.SignUpResponse;
import com.studygather.user.entity.UserRole;
import com.studygather.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void loginSucceedsWithCorrectCredentials() {
        SignUpResponse signUpResponse = userService.signUp(new SignUpRequest(
                "login@example.com",
                "password123",
                "login-user"
        ));
        LoginRequest request = new LoginRequest("login@example.com", "password123");

        LoginResponse response = authService.login(request);

        assertTrue(jwtTokenProvider.validateToken(response.accessToken()));
        assertEquals(signUpResponse.id(), jwtTokenProvider.getUserId(response.accessToken()));
        assertEquals(UserRole.USER, jwtTokenProvider.getRole(response.accessToken()));
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
