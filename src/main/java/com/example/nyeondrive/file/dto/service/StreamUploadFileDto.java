package com.example.nyeondrive.file.dto.service;

public record StreamUploadFileDto(
        String contentType,
        Long contentLength,
        boolean isTrashed
) {
}
