package com.example.nyeondrive.service;

import com.example.nyeondrive.constant.FileType;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.repository.FileRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void saveFile(File file) {
        if (!file.parentIsDirectory()) {
            throw new IllegalArgumentException("Parent file is not a directory");
        }
        fileRepository.saveFile(file);
    }

    public Optional<File> findFile(Long fileId) {
        return fileRepository.findFile(fileId);
    }
}
