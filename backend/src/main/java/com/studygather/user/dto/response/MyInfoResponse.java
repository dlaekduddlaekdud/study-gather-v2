package com.studygather.user.dto.response;

import com.studygather.user.entity.User;
import com.studygather.user.entity.UserRole;

public record MyInfoResponse(
        Long id,
        String email,
        String nickname,
        UserRole role
) {

    public static MyInfoResponse from(User user) {
        return new MyInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }
}
