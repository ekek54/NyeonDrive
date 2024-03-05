package com.example.nyeondrive.file.dto.response;

import com.example.nyeondrive.file.entity.File;
import java.time.LocalDateTime;


public record FileResponseDto(
        Long id,
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        LocalDateTime createdAt
) {
    public static FileResponseDto of(File file) {
        return new FileResponseDto(file.getId(), file.getFileName().toString(), file.getParentId().orElse(null),
                file.getContentType(), file.isTrashed(), file.getCreatedAt());
    }
}
