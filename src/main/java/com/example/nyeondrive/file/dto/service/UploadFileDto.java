package com.example.nyeondrive.file.dto.service;

public record UploadFileDto(
        Long id,
        String contentType,
        Long contentLength,
        boolean isTrashed
) {
}
