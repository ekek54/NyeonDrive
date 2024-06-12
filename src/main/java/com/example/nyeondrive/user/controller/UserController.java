package com.example.nyeondrive.user.controller;

import com.example.nyeondrive.user.dto.request.CreateUserRequestDto;
import com.example.nyeondrive.user.dto.response.UserResponseDto;
import com.example.nyeondrive.user.dto.service.CreateUserDto;
import com.example.nyeondrive.user.entity.User;
import com.example.nyeondrive.user.service.UserService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Validated @RequestBody CreateUserRequestDto createUserRequestDto
    ) {
        CreateUserDto createUserDto = createUserRequestDto.toCreateUserDto();
        User createdUser = userService.createUser(createUserDto);
        return ResponseEntity.ok()
                .body(UserResponseDto.of(createdUser));
    }

    @GetMapping
    public String getUser(
            @AuthenticationPrincipal UUID userId
    ) {
        log.debug("userId: {}", userId);
        return userId.toString();
    }
}
