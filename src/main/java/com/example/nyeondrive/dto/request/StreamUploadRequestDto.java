package com.example.nyeondrive.dto.request;

import lombok.Builder;

public record StreamUploadRequestDto(
        String fileName,
        String contentType,
        Long contentLength,
        Long parentId
) {
    @Builder
    public StreamUploadRequestDto(String fileName, String contentType, Long contentLength, Long parentId) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.parentId = parentId;
    }
}
