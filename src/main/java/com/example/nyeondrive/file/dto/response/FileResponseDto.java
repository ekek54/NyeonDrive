package com.example.nyeondrive.file.dto.response;

import com.example.nyeondrive.file.dto.service.FileDto;
import java.time.LocalDateTime;


public record FileResponseDto(
        Long id,
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        LocalDateTime createdAt
) {
    public static FileResponseDto of(FileDto fileDto) {
        return new FileResponseDto(fileDto.id(), fileDto.name(), fileDto.parentId(), fileDto.contentType(), fileDto.isTrashed(), fileDto.createdAt());
    }
}
