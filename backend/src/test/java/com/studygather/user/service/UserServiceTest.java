package com.studygather.user.service;

import com.studygather.user.dto.request.SignUpRequest;
import com.studygather.user.dto.response.MyInfoResponse;
import com.studygather.user.dto.response.SignUpResponse;
import com.studygather.user.entity.User;
import com.studygather.user.entity.UserRole;
import com.studygather.user.exception.DuplicateEmailException;
import com.studygather.user.exception.UserNotFoundException;
import com.studygather.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signUpEncryptsPasswordAndSavesUser() {
        SignUpRequest request = new SignUpRequest(
                "signup@example.com",
                "password123",
                "signup-user"
        );

        SignUpResponse response = userService.signUp(request);
        User savedUser = userRepository.findByEmail(request.email()).orElseThrow();

        assertEquals(request.email(), response.email());
        assertEquals(request.nickname(), response.nickname());
        assertEquals(UserRole.USER, response.role());
        assertNotEquals(request.password(), savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(request.password(), savedUser.getPasswordHash()));
    }

    @Test
    void signUpRejectsDuplicateEmail() {
        SignUpRequest request = new SignUpRequest(
                "duplicate@example.com",
                "password123",
                "first-user"
        );
        userService.signUp(request);

        SignUpRequest duplicateRequest = new SignUpRequest(
                request.email(),
                "different-password",
                "second-user"
        );

        assertThrows(
                DuplicateEmailException.class,
                () -> userService.signUp(duplicateRequest)
        );
    }

    @Test
    void getMyInfoReturnsUserInformation() {
        SignUpResponse signUpResponse = userService.signUp(new SignUpRequest(
                "my-info@example.com",
                "password123",
                "my-info-user"
        ));

        MyInfoResponse response = userService.getMyInfo(signUpResponse.id());

        assertEquals(signUpResponse.id(), response.id());
        assertEquals(signUpResponse.email(), response.email());
        assertEquals(signUpResponse.nickname(), response.nickname());
        assertEquals(UserRole.USER, response.role());
    }

    @Test
    void getMyInfoRejectsUnknownUser() {
        assertThrows(
                UserNotFoundException.class,
                () -> userService.getMyInfo(Long.MAX_VALUE)
        );
    }
}
