package com.example.nyeondrive.controller;

import com.example.nyeondrive.dto.request.FileRequestDto;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.service.FileService;
import com.example.nyeondrive.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;
    private final StorageService storageService;

    public FileController(FileService fileService, StorageService storageService) {
        this.fileService = fileService;
        this.storageService = storageService;
    }


    @PostMapping
    public String saveFile(@RequestBody FileRequestDto fileRequestDto) {
        File file = File.builder()
                .fileName(fileRequestDto.getName())
                .parent(fileService.findFile(fileRequestDto.getParentId()).orElseThrow(RuntimeException::new))
                .build();
        fileService.saveFile(file);
        return "saveFile";
    }

    @PostMapping(value = "/upload", params = "mode=stream")
    public String streamUpload(
            @RequestHeader("File-Name") String fileName,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Content-Length") Long contentLength,
            @RequestHeader("Parent-Id") Long parentId,
            HttpServletRequest request
    ) throws IOException {
        File parent = fileService.findFile(parentId).orElseThrow(RuntimeException::new);
        File file = File.builder()
                .fileName(fileName)
                .contentType(contentType)
                .size(contentLength)
                .parent(parent)
                .inputStream(request.getInputStream())
                .build();
        fileService.saveFile(file);
        storageService.uploadFile(file);
        return "streamUpload";
    }
}
