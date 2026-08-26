package com.studygather.auth.controller;

import com.studygather.auth.service.AuthService;
import com.studygather.common.api.ApiResponse;
import com.studygather.user.dto.request.LoginRequest;
import com.studygather.user.dto.request.SignUpRequest;
import com.studygather.user.dto.response.SignUpResponse;
import com.studygather.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("로그인에 성공했습니다.", null));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }
}
