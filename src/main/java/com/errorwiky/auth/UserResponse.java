package com.errorwiky.auth;

import com.errorwiky.user.UserEntity;

public record UserResponse(Long userId, String loginId, String name, String email,
                           String role, String provider) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getLoginId(), user.getName(), user.getEmail(),
                user.getRole().name(), user.getProvider().name());
    }
}
