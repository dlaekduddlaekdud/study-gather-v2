package com.studygather.user.service;

import com.studygather.user.dto.request.SignUpRequest;
import com.studygather.user.dto.response.SignUpResponse;
import com.studygather.user.entity.User;
import com.studygather.user.exception.DuplicateEmailException;
import com.studygather.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), passwordHash, request.nickname());
        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }
}
