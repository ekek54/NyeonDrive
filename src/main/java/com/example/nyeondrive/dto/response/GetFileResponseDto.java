package com.example.nyeondrive.dto.response;

import com.example.nyeondrive.entity.File;
import java.time.LocalDateTime;


public record GetFileResponseDto(
        Long id,
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed,
        LocalDateTime createdAt
) {
    public static GetFileResponseDto of(File file) {
        return new GetFileResponseDto(file.getId(), file.getFileName().toString(), file.getParent().getId(),
                file.getContentType(), file.isTrashed(), file.getCreatedAt());
    }
}
