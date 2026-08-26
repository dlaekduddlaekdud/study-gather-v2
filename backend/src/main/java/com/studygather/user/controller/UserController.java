package com.studygather.user.controller;

import com.studygather.common.api.ApiResponse;
import com.studygather.user.dto.response.MyInfoResponse;
import com.studygather.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyInfoResponse>> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        MyInfoResponse response = userService.getMyInfo(userId);

        return ResponseEntity.ok(ApiResponse.success("내 정보를 조회했습니다.", response));
    }
}
