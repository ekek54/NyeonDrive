package com.example.nyeondrive.service;

import com.example.nyeondrive.constant.FileType;
import com.example.nyeondrive.dto.request.FileRequestDto;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.repository.FileRepository;
import com.example.nyeondrive.vo.FileName;
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
        fileRepository.saveFile(file);
    }

    public void saveFile(FileRequestDto fileRequestDto) {
        File parent = findFile(fileRequestDto.getParentId());
        File file = File.builder().fileName(fileRequestDto.getName()).parent(parent).build();
        fileRepository.saveFile(file);
    }

    public File findFile(Long fileId) {
        return fileRepository.findFile(fileId).orElseThrow(() -> new RuntimeException("File not found"));
    }
    }
}
