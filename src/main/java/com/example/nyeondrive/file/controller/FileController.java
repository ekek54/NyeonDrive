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
import com.example.nyeondrive.file.dto.service.UpdateFileDto;
import com.example.nyeondrive.file.service.FileService;
import com.example.nyeondrive.file.service.StorageService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<FileResponseDto> createFile(
            @Validated @RequestBody CreateFileRequestDto createFileRequestDto) {
        CreateFileDto createFileDto = createFileRequestDto.toCreateFileDto();
        FileDto fileDto = fileService.createFile(createFileDto);
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
            @RequestBody UpdateFileRequestDto updateFileRequestDto
    ) {
        UpdateFileDto updateFileDto = updateFileRequestDto.toUpdateFileDto();
        FileDto fileDto = fileService.updateFile(fileId, updateFileDto);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }

//    @PostMapping(value = "/upload", params = "mode=stream")
//    public String streamUpload(
//            @RequestHeader("File-Name") String fileName,
//            @RequestHeader("Content-Type") String contentType,
//            @RequestHeader("Content-Length") Long contentLength,
//            @RequestHeader("Parent-Id") Long parentId,
//            HttpServletRequest request
//    ) throws IOException {
//        File parent = fileService.findFile(parentId);
//        File file = File.builder()
//                .fileName(fileName)
//                .contentType(contentType)
//                .size(contentLength)
//                .parent(parent)
//                .inputStream(request.getInputStream())
//                .build();
//        fileService.createFile(file);
//        storageService.uploadFile(file);
//        return "streamUpload";
//    }

    @GetMapping(path = "/{fileId}")
    public ResponseEntity<FileResponseDto> getFile(@PathVariable("fileId") Long fileId) {
        FileDto fileDto = fileService.findFile(fileId);
        return ResponseEntity.ok()
                .body(FileResponseDto.of(fileDto));
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDto>> listFile(@ModelAttribute ListFileRequestDto ListFileRequestDto) {
        FileFilterDto fileFilterDto = ListFileRequestDto.toFileFilterDto();
        FilePagingDto filePagingDto = ListFileRequestDto.toFilePagingDto();
        List<FileOrderDto> fileOrderDtos = ListFileRequestDto.toFileOrderDtos();
        List<FileResponseDto> files = fileService.listFile(fileFilterDto, filePagingDto, fileOrderDtos).stream()
                .map(FileResponseDto::of)
                .toList();
        return ResponseEntity.ok()
                .body(files);
    }
//
//    @DeleteMapping(path = "/{fileId}")
//    public ResponseEntity<Void> deleteFile(@PathVariable("fileId") Long fileId) {
//        fileService.deleteFile(fileId);
//        return ResponseEntity.noContent()
//                .build();
//    }
}
