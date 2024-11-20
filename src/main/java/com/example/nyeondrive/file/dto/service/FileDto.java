package com.example.nyeondrive.file.dto.service;

import com.example.nyeondrive.file.entity.File;
import java.time.LocalDateTime;
import java.util.UUID;

public record FileDto(
        Long id,
        UUID ownerId,
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        LocalDateTime createdAt
) {
    public static FileDto of(File file) {
        return new FileDto(
                file.getId(),
                file.getOwnerId(),
                file.getFileName().toString(),
                file.getParentId(),
                file.getContentType(),
                file.isTrashed(),
                file.getCreatedAt()
        );
    }
}
