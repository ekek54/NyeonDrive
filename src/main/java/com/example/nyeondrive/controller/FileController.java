package com.example.nyeondrive.controller;

import com.example.nyeondrive.dto.service.CreateFileDto;
import com.example.nyeondrive.dto.service.FileFilterDto;
import com.example.nyeondrive.dto.service.FilePagingDto;
import com.example.nyeondrive.dto.request.CreateFileRequestDto;
import com.example.nyeondrive.dto.request.ListFileRequestDto;
import com.example.nyeondrive.dto.request.UpdateFileRequestDto;
import com.example.nyeondrive.dto.response.GetFileResponseDto;
import com.example.nyeondrive.entity.File;
import com.example.nyeondrive.service.FileService;
import com.example.nyeondrive.service.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@Validated
public class FileController {
    private final FileService fileService;
    private final StorageService storageService;

    public FileController(FileService fileService, StorageService storageService) {
        this.fileService = fileService;
        this.storageService = storageService;
    }


    @PostMapping
    public String createFile(@Valid @RequestBody CreateFileRequestDto createFileRequestDto) {
        CreateFileDto createFileDto = createFileRequestDto.toCreateFileDto();
        fileService.createFile(createFileDto);
        return "createFile";
    }

    @PatchMapping(path = "/{fileId}")
    public String updateFile(
            @PathVariable("fileId") Long fileId,
            @RequestBody UpdateFileRequestDto updateFileRequestDto
    ) {
        fileService.updateFile(fileId, updateFileRequestDto);
        return "updateFile";
    }

    @PostMapping(value = "/upload", params = "mode=stream")
    public String streamUpload(
            @RequestHeader("File-Name") String fileName,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Content-Length") Long contentLength,
            @RequestHeader("Parent-Id") Long parentId,
            HttpServletRequest request
    ) throws IOException {
        File parent = fileService.findFile(parentId);
        File file = File.builder()
                .fileName(fileName)
                .contentType(contentType)
                .size(contentLength)
                .parent(parent)
                .inputStream(request.getInputStream())
                .build();
        fileService.createFile(file);
        storageService.uploadFile(file);
        return "streamUpload";
    }

    @PostMapping(value = "/createRootFolder")
    public String createRootFolder() {
        fileService.createRootFolder();
        return "createRootFolder";
    }

    @GetMapping(path = "/{fileId}")
    public GetFileResponseDto getFile(@PathVariable("fileId") Long fileId) {
        return GetFileResponseDto.of(fileService.findFile(fileId));
    }

    @GetMapping
    public List<GetFileResponseDto> listFile(@ModelAttribute ListFileRequestDto ListFileRequestDto) {
        FileFilterDto fileFilterDto = ListFileRequestDto.toFileFilterDto();
        FilePagingDto filePagingDto = ListFileRequestDto.toFilePagingDto();
        return fileService.listFile(fileFilterDto, filePagingDto).stream().map(GetFileResponseDto::of).toList();
    }

}
