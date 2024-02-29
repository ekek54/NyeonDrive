package com.example.nyeondrive.dto.response;

import com.example.nyeondrive.entity.File;


public record GetFileResponseDto(
        Long id,
        String name,
        Long parentId,
        String contentType,
        Boolean isTrashed
) {
    public static GetFileResponseDto of(File file) {
        return new GetFileResponseDto(file.getId(), file.getFileName().toString(), file.getParent().getId(),
                file.getContentType(), file.isTrashed());
    }
}
