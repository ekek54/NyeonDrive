package com.example.nyeondrive.service;

import com.example.nyeondrive.dto.request.CreateFileRequestDto;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.repository.FileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void saveFile(File file) {
        fileRepository.saveFile(file);
    }

    public void saveFile(CreateFileRequestDto createFileRequestDto) {
        File parent = findFile(createFileRequestDto.getParentId());
        File file = File.builder()
                .fileName(createFileRequestDto.getName())
                .contentType(createFileRequestDto.getContentType())
                .parent(parent)
                .build();
        fileRepository.saveFile(file);
    }

    public File findFile(Long fileId) {
        return fileRepository.findFile(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void createRootFolder() {
        File root = File.createRootFolder();
        fileRepository.saveFile(root);
    }
}
