package com.example.nyeondrive.service;

import com.example.nyeondrive.dto.request.CreateFileRequestDto;
import com.example.nyeondrive.dto.request.UpdateFileRequestDto;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.repository.JpaFileRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FileService {
    private final JpaFileRepository jpaFileRepository;

    public FileService(JpaFileRepository jpaFileRepository) {
        this.jpaFileRepository = jpaFileRepository;
    }

    public void saveFile(File file) {
        jpaFileRepository.saveFile(file);
    }

    public void saveFile(CreateFileRequestDto createFileRequestDto) {
        File parent = findFile(createFileRequestDto.getParentId());
        File file = File.builder()
                .fileName(createFileRequestDto.getName())
                .contentType(createFileRequestDto.getContentType())
                .parent(parent)
                .build();
        jpaFileRepository.saveFile(file);
    }

    public File findFile(Long fileId) {
        return jpaFileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void createRootFolder() {
        File root = File.createRootFolder();
        jpaFileRepository.saveFile(root);
    }

public void updateFile(Long fileId, UpdateFileRequestDto updateFileRequestDto) {
        File file = findFile(fileId);
        if (updateFileRequestDto.getName() != null) {
            file.setName(updateFileRequestDto.getName());
        }
        if (updateFileRequestDto.getParentId() != null) {
            File parent = findFile(updateFileRequestDto.getParentId());
            file.setParent(parent);
        }
        if (updateFileRequestDto.getContentType() != null) {
            file.setContentType(updateFileRequestDto.getContentType());
        }
        if (updateFileRequestDto.getIsTrashed() != null) {
            file.setTrashed(updateFileRequestDto.getIsTrashed());
        }
        jpaFileRepository.saveFile(file);
    }
}
