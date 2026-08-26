package com.studygather.user.dto.response;

import com.studygather.user.entity.User;
import com.studygather.user.entity.UserRole;

public record SignUpResponse(
        Long id,
        String email,
        String nickname,
        UserRole role
) {

    public static SignUpResponse from(User user) {
        return new SignUpResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}
