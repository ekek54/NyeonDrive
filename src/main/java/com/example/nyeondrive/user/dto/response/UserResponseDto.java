package com.example.nyeondrive.user.dto.response;

import com.example.nyeondrive.user.entity.User;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Long driveId
) {

    public static UserResponseDto of(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), user.getDrive().getId());
    }
}
