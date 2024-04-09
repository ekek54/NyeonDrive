package com.example.nyeondrive.file.dto.request;

import lombok.Builder;
@Builder
public record StreamUploadRequestDto(
        String fileName,
        String contentType,
        Long contentLength,
        Long parentId
) {
}
