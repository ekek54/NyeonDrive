package com.example.nyeondrive.user.dto.request;

import com.example.nyeondrive.user.dto.service.CreateUserDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequestDto(
    @NotNull
    String name,
    @Email
    String email
) {
    public CreateUserDto toCreateUserDto() {
        return new CreateUserDto(
            name,
            email
        );
    }
}
