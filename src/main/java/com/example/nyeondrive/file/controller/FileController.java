package com.example.nyeondrive.file.controller;

import com.example.nyeondrive.file.dto.request.CreateFileRequestDto;
import com.example.nyeondrive.file.dto.request.ListFileRequestDto;
import com.example.nyeondrive.file.dto.request.UpdateFileRequestDto;
import com.example.nyeondrive.file.dto.response.FileResponseDto;
import com.example.nyeondrive.file.dto.service.CreateFileDto;
import com.example.nyeondrive.file.dto.service.FileDto;
import com.example.nyeondrive.file.dto.service.FileFilterDto;
import com.example.nyeondrive.file.dto.service.FileOrderDto;
import com.example.nyeondrive.file.dto.service.FilePagingDto;
import com.example.nyeondrive.file.dto.service.StreamUploadFileDto;
import com.example.nyeondrive.file.dto.service.UpdateFileDto;
import com.example.nyeondrive.file.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Slf4j
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ResponseEntity<FileResponseDto> createFile(
            @Validated @RequestBody CreateFileRequestDto createFileRequestDto,
            @AuthenticationPrincipal UUID userId
    ) {
        CreateFileDto createFileDto = createFileRequestDto.toCreateFileDto();
        FileDto fileDto = fileService.createFile(createFileDto, userId);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }

    @GetMapping(value = "/drive")
    public ResponseEntity<FileResponseDto> getDrive(
            @AuthenticationPrincipal UUID userId
    ) {
        FileDto fileDto = fileService.findDrive(userId);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }

    @PostMapping(value = "/drive")
    public ResponseEntity<FileResponseDto> createDrive(
            @AuthenticationPrincipal UUID userId
    ) {
        FileDto fileDto = fileService.createDrive(userId);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }


    @PatchMapping(path = "/{fileId}")
    public ResponseEntity<FileResponseDto> updateFile(
            @PathVariable("fileId") Long fileId,
            @RequestBody UpdateFileRequestDto updateFileRequestDto,
            @AuthenticationPrincipal UUID userId
    ) {
        UpdateFileDto updateFileDto = updateFileRequestDto.toUpdateFileDto();
        FileDto fileDto = fileService.updateFile(fileId, updateFileDto, userId);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }

    @PostMapping(params = "uploadType=stream")
    public ResponseEntity<FileResponseDto> streamUpload(
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader("Content-Length") Long contentLength,
            HttpServletRequest request,
            @AuthenticationPrincipal UUID userId
    ) {
        StreamUploadFileDto streamUploadFileDto = new StreamUploadFileDto(
                contentType,
                contentLength,
                false
        );
        try {
            FileDto tmpFile = fileService.streamUploadFile(streamUploadFileDto, request.getInputStream(), userId);
            return ResponseEntity.ok()
                    .body(FileResponseDto.of(tmpFile));
        } catch (IOException e) {
            throw new IllegalStateException("Error occurred while read file stream", e);
        }
    }

    @GetMapping(path = "/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(
            @PathVariable("fileId") Long fileId,
            @AuthenticationPrincipal UUID userId
    ) {
        FileDto fileDto = fileService.findFile(fileId, userId);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDto>> listFile(
            @ModelAttribute ListFileRequestDto ListFileRequestDto,
            @AuthenticationPrincipal UUID userId
    ) {
        FileFilterDto fileFilterDto = ListFileRequestDto.toFileFilterDto();
        FilePagingDto filePagingDto = ListFileRequestDto.toFilePagingDto();
        List<FileOrderDto> fileOrderDtos = ListFileRequestDto.toFileOrderDtos();
        List<FileResponseDto> files = fileService.listFile(fileFilterDto, filePagingDto, fileOrderDtos, userId)
                .stream()
                .map(FileResponseDto::of)
                .toList();
        return ResponseEntity.ok()
                .body(files);
    }

    @DeleteMapping(path = "/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable("fileId") Long fileId,
            @AuthenticationPrincipal UUID userId
    ) {
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent()
                .build();
    }
}
