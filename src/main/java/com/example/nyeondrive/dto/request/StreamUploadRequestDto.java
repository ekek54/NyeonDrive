package com.example.nyeondrive.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StreamUploadRequestDto {
    private final String fileName;
    private final String contentType;
    private final Long contentLength;
    private final Long parentId;

    @Builder
    public StreamUploadRequestDto(String fileName, String contentType, Long contentLength, Long parentId) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.parentId = parentId;
    }
}
