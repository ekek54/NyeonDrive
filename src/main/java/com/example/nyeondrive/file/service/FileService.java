package com.example.nyeondrive.file.service;

import com.example.nyeondrive.exception.error.NotFoundException;
import com.example.nyeondrive.file.dto.service.CreateFileDto;
import com.example.nyeondrive.file.dto.service.FileFilterDto;
import com.example.nyeondrive.file.dto.service.FileOrderDto;
import com.example.nyeondrive.file.dto.service.FilePagingDto;
import com.example.nyeondrive.file.dto.service.UpdateFileDto;
import com.example.nyeondrive.file.entity.File;
import com.example.nyeondrive.file.repository.FileRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public void createFile(File file) {
        fileRepository.save(file);
    }

    public File createFile(CreateFileDto createFileDto) {
        File parent = findFile(createFileDto.parentId());
        File file = File.builder()
                .fileName(createFileDto.name())
                .contentType(createFileDto.contentType())
                .parent(parent)
                .isTrashed(createFileDto.isTrashed())
                .build();
        fileRepository.save(file);
        return file;
    }

    public File findFile(Long fileId) {
        return fileRepository.findById(fileId).orElseThrow(() -> new NotFoundException("File not found"));
    }

    public File createRootFolder() {
        File root = File.createRootFolder();
        return fileRepository.save(root);
    }

    public File updateFile(Long fileId, UpdateFileDto updateFileDto) {
        File file = findFile(fileId);
        if (updateFileDto.name() != null) {
            file.setFileName(updateFileDto.name());
        }
        if (updateFileDto.parentId() != null) {
            File parent = findFile(updateFileDto.parentId());
            file.setParent(parent);
        }
        if (updateFileDto.contentType() != null) {
            file.setContentType(updateFileDto.contentType());
        }
        if (updateFileDto.isTrashed() != null) {
            file.setTrashed(updateFileDto.isTrashed());
        }
        fileRepository.save(file);
        return file;
    }

    public List<File> listFile(FileFilterDto fileFilterDto, FilePagingDto filePagingDto, List<FileOrderDto> fileOrderDtos) {
        return fileRepository.findAll(fileFilterDto, filePagingDto, fileOrderDtos);
    }

    public void deleteFile(Long fileId) {
        File file = findFile(fileId);
        fileRepository.delete(file);
    }
}
