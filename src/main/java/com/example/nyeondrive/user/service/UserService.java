package com.example.nyeondrive.user.service;

import com.example.nyeondrive.exception.error.BadRequestException;
import com.example.nyeondrive.file.entity.File;
import com.example.nyeondrive.file.service.FileService;
import com.example.nyeondrive.user.dto.service.CreateUserDto;
import com.example.nyeondrive.user.entity.User;
import com.example.nyeondrive.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class UserService {
    private final UserRepository userRepository;
    private final FileService fileService;

    public UserService(UserRepository userRepository, FileService fileService) {
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public User createUser(CreateUserDto createUserDto) {
        if (isDuplicateName(createUserDto.name())) {
            throw new BadRequestException("Name is already taken");
        }
        User user = User.builder()
                .name(createUserDto.name())
                .email(createUserDto.email())
                .build();
        File rootFolder = fileService.createRootFolder();
        user.setDrive(rootFolder);
        return userRepository.save(user);
    }

    private boolean isDuplicateName(String name) {
        return userRepository.findByName(name).isPresent();
    }
}
